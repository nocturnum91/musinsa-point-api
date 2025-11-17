package com.musinsa.point.application.point;

import com.musinsa.point.api.point.dto.request.PointSaveCancelRequest;
import com.musinsa.point.api.point.dto.request.PointSaveRequest;
import com.musinsa.point.api.point.dto.request.PointUseCancelRequest;
import com.musinsa.point.api.point.dto.request.PointUseRequest;
import com.musinsa.point.api.point.dto.response.PointSaveCancelResponse;
import com.musinsa.point.api.point.dto.response.PointSaveResponse;
import com.musinsa.point.api.point.dto.response.PointUseCancelResponse;
import com.musinsa.point.api.point.dto.response.PointUseResponse;
import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.member.repository.MemberRepository;
import com.musinsa.point.domain.point.entity.*;
import com.musinsa.point.domain.point.model.ExpireType;
import com.musinsa.point.domain.point.model.PointUseStatus;
import com.musinsa.point.domain.point.repository.*;
import com.musinsa.point.global.exception.BusinessException;
import com.musinsa.point.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private static final String POLICY_MAX_SAVE_PER_REQUEST = "MAX_SAVE_PER_REQUEST";
    private static final String POLICY_MAX_FREE_POINT_BALANCE = "MAX_FREE_POINT_BALANCE";

    private final MemberRepository memberRepository;
    private final PointItemRepository pointItemRepository;
    private final PointSystemPolicyRepository pointSystemPolicyRepository;
    private final MemberPointLimitRepository memberPointLimitRepository;
    private final PointSaveRepository pointSaveRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final PointUseRepository pointUseRepository;
    private final PointUseDetailRepository pointUseDetailRepository;

    public PointSaveResponse savePoint(PointSaveRequest request) {

        // 1. 회원 조회
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 아이템 조회 (적립의 기준)
        PointItem pointItem = pointItemRepository.findById(request.getItemNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_ITEM_NOT_FOUND));

        // 3. 적립 금액 결정 (요청값 우선, 없으면 default_amount)
        long amount = Optional.ofNullable(request.getAmount())
                .orElse(pointItem.getDefaultAmount());

        if (amount < 1) {
            throw new BusinessException(
                    ErrorCode.POINT_POLICY_VIOLATION,
                    "적립 포인트는 1 이상이어야 합니다."
            );
        }

        // 4. 1회 최대 적립 포인트 정책 체크
        long maxSavePerRequest = getLongPolicy(POLICY_MAX_SAVE_PER_REQUEST);
        if (amount > maxSavePerRequest) {
            throw new BusinessException(
                    ErrorCode.POINT_POLICY_VIOLATION,
                    "1회 최대 적립 가능 포인트(" + maxSavePerRequest + ")를 초과했습니다."
            );
        }

        // 5. 현재 보유 잔액 + 이번 적립 후 잔액 계산
        long currentBalance = Optional.ofNullable(pointSaveRepository.sumAvailableAmountByMember(member))
                .orElse(0L);
        long nextBalance = currentBalance + amount;

        // 6. 전역 최대 보유 한도 정책 + 회원 개별 한도 체크
        long globalMaxBalance = getLongPolicy(POLICY_MAX_FREE_POINT_BALANCE);
        long memberMaxBalance = memberPointLimitRepository.findById(member.getMemberNo())
                .map(MemberPointLimit::getMaxBalance)
                .orElse(globalMaxBalance);

        if (nextBalance > memberMaxBalance) {
            throw new BusinessException(
                    ErrorCode.POINT_BALANCE_EXCEEDED,
                    "회원 최대 보유 가능 포인트(" + memberMaxBalance + ")를 초과합니다."
            );
        }

        // 7. 만료일 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = calculateExpireAt(now, pointItem);

        // 8. 수기 지급 여부는 아이템에서 판단
        boolean manual = "Y".equalsIgnoreCase(pointItem.getIsManualYn());

        // 9. point_save 생성/저장 (이벤트는 메타 정보로 eventCode만 보관)
        PointSave save = PointSave.createSave(
                member,
                pointItem,
                pointItem.getPointType(),
                amount,
                expireAt,
                manual,
                request.getEventCode()  // null 가능
        );
        pointSaveRepository.save(save);

        // 10. history 생성/저장 (ledger)
        String desc = buildSaveDescription(pointItem);
        PointHistory history = PointHistory.createSaveHistory(
                member,
                save,
                amount,       // SAVE → +amount
                nextBalance,
                now,
                desc
        );
        pointHistoryRepository.save(history);

        // 11. 응답
        return PointSaveResponse.of(save, history);
    }

    @Transactional
    public PointSaveCancelResponse cancelSave(PointSaveCancelRequest request) {

        // 1. 회원 조회
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. save 조회
        PointSave save = pointSaveRepository.findById(request.getSaveNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_SAVE_NOT_FOUND));

        if (!save.getMember().equals(member)) {
            throw new BusinessException(ErrorCode.POINT_SAVE_NOT_FOUND, "해당 회원의 적립 건이 아닙니다.");
        }

        // 3. 이미 일부라도 사용되었는지 확인
        Long usedAmount = pointUseDetailRepository.getUsedAmount(save.getSaveNo());
        if (usedAmount > 0) {
            throw new BusinessException(ErrorCode.POINT_USE_CANCEL_INVALID,
                    "이미 사용된 적립금은 취소할 수 없습니다.");
        }

        long amount = save.getAmount();

        // 4. 포인트 감소 (available_amount → 0)
        long currentBalance = pointSaveRepository.sumAvailableAmountByMember(member);
        long nextBalance = currentBalance - amount;

        save.cancelSave(); // available_amount = 0 으로 만드는 도메인 메서드

        // 5. history 저장
        LocalDateTime now = LocalDateTime.now();
        PointHistory history = PointHistory.createSaveCancelHistory(
                member,
                save,
                -amount,     // 취소 → 음수
                nextBalance,
                now,
                "적립 취소: saveNo=" + save.getSaveNo()
        );
        pointHistoryRepository.save(history);

        // 6. 응답
        return PointSaveCancelResponse.builder()
                .saveNo(save.getSaveNo())
                .pointKey(history.getHistoryNo())
                .canceledAmount(amount)
                .balanceAfter(nextBalance)
                .occurredAt(now)
                .build();
    }


    @Transactional
    public PointUseResponse usePoint(PointUseRequest request) {

        // 1. 회원 조회
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        long useAmount = request.getUseAmount();

        // 2. 잔액 확인
        long currentBalance = Optional.ofNullable(pointSaveRepository.sumAvailableAmountByMember(member))
                .orElse(0L);

        if (currentBalance < useAmount) {
            throw new BusinessException(
                    ErrorCode.POINT_BALANCE_LACK,
                    "포인트 잔액이 부족합니다. 현재 사용 가능 잔액: " + currentBalance
            );
        }

        // 3. 우선순위 저장 목록 조회 (manual → expire 임박 → 오래된 순)
        List<PointSave> buckets = pointSaveRepository.findUsableBuckets(member);

        long remaining = useAmount;

        // 4. point_use 생성
        PointUse pointUse = PointUse.createUse(member, request.getOrderNo(), useAmount);
        pointUseRepository.save(pointUse);

        List<PointUseDetail> details = new ArrayList<>();

        // 5. 버킷 순회하며 차감
        for (PointSave save : buckets) {
            if (remaining == 0) break;

            long canUse = save.getAvailableAmount();
            long used = Math.min(canUse, remaining);

            // 버킷 차감
            save.use(used);

            // detail 기록
            PointUseDetail detail = PointUseDetail.create(pointUse, save, used);
            pointUseDetailRepository.save(detail);
            details.add(detail);

            remaining -= used;
        }

        // sanity check
        if (remaining > 0) {
            throw new BusinessException(
                    ErrorCode.POINT_BALANCE_LACK,
                    "사용 가능한 포인트가 부족합니다."
            );
        }

        // 6. history 기록
        long nextBalance = currentBalance - useAmount;
        LocalDateTime now = LocalDateTime.now();

        PointHistory history = PointHistory.createUseHistory(
                member,
                pointUse,
                -useAmount,            // 사용 → 음수
                nextBalance,
                now,
                "주문번호 " + request.getOrderNo() + " 사용"
        );

        pointHistoryRepository.save(history);

        // 7. 응답
        return PointUseResponse.builder()
                .useNo(pointUse.getUseNo())
                .pointKey(history.getHistoryNo())
                .usedAmount(useAmount)
                .balanceAfter(nextBalance)
                .orderNo(pointUse.getOrderNo())
                .occurredAt(now)
                .build();
    }

    @Transactional
    public PointUseCancelResponse cancelUse(PointUseCancelRequest request) {

        // 1. 회원 조회
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 사용(use) 조회
        PointUse use = pointUseRepository.findById(request.getUseNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_USE_NOT_FOUND));

        if (!use.getMember().equals(member)) {
            throw new BusinessException(ErrorCode.POINT_USE_NOT_FOUND, "해당 회원의 사용 건이 아닙니다.");
        }

        // 3. 총 취소 가능 금액 계산
        long totalCancelable = pointUseDetailRepository.getCancelableAmountByUse(use.getUseNo());
        if (totalCancelable <= 0) {
            throw new BusinessException(ErrorCode.POINT_USE_CANCEL_INVALID, "이미 전액 취소된 사용 건입니다.");
        }

        long requestAmount = request.getCancelAmount();
        if (requestAmount > totalCancelable) {
            throw new BusinessException(ErrorCode.POINT_USE_CANCEL_INVALID,
                    "취소 가능 금액(" + totalCancelable + ")을 초과합니다.");
        }

        long remainingCancel = requestAmount;

        long currentBalance = pointSaveRepository.sumAvailableAmountByMember(member);
        long nextBalance = currentBalance;
        LocalDateTime now = LocalDateTime.now();

        // 4. 사용 detail 오래된 순으로 조회
        List<PointUseDetail> details = pointUseDetailRepository.findByPointUseOrderByDetailNoAsc(use);

        List<PointHistory> historyList = new ArrayList<>();

        for (PointUseDetail detail : details) {

            long used = detail.getUsedAmount();
            long alreadyCanceled = detail.getCanceledAmount();
            long cancelable = used - alreadyCanceled;

            if (cancelable <= 0) continue;

            long cancelThisTime = Math.min(remainingCancel, cancelable);
            remainingCancel -= cancelThisTime;

            PointSave save = detail.getPointSave();
            PointItem item = save.getPointItem();

            // CASE 1 — save가 만료된 상태
            if (save.getExpireAt().isBefore(now)) {

                // expire policy 기반 만료일 재계산
                LocalDateTime newExpire = calculateExpireAt(now, item);

                // 이벤트 없이 적립되므로 eventCode = null
                PointSave refundSave = PointSave.createSave(
                        member,
                        item,
                        item.getPointType(),
                        cancelThisTime,
                        newExpire,
                        true,
                        null
                );
                pointSaveRepository.save(refundSave);

                nextBalance += cancelThisTime;

                PointHistory saveHistory = PointHistory.createSaveHistory(
                        member,
                        refundSave,
                        cancelThisTime,
                        nextBalance,
                        now,
                        "만료된 적립에 대한 사용 취소로 신규 적립 처리"
                );
                pointHistoryRepository.save(saveHistory);
                historyList.add(saveHistory);

            } else {
                // CASE 2 — save가 아직 유효 → available_amount 증가
                save.increaseAvailableAmount(cancelThisTime);

                nextBalance += cancelThisTime;

                PointHistory cancelHistory = PointHistory.createUseCancelHistory(
                        member,
                        use,
                        cancelThisTime,
                        nextBalance,
                        now,
                        "포인트 사용 취소"
                );
                pointHistoryRepository.save(cancelHistory);
                historyList.add(cancelHistory);
            }

            // detail 취소 금액 증가
            detail.addCanceledAmount(cancelThisTime);

            if (remainingCancel == 0) break;
        }

        // 5. use 상태 갱신
        if (requestAmount == totalCancelable) {
            use.changeStatus(PointUseStatus.CANCEL);
        } else {
            use.changeStatus(PointUseStatus.PARTIAL_CANCEL);
        }

        // 대표 history (마지막 생성된 기록)
        PointHistory lastHistory = historyList.get(historyList.size() - 1);

        return PointUseCancelResponse.builder()
                .useNo(use.getUseNo())
                .pointKey(lastHistory.getHistoryNo())
                .canceledAmount(requestAmount)
                .balanceAfter(nextBalance)
                .orderNo(use.getOrderNo())
                .occurredAt(lastHistory.getOccurredAt())
                .build();
    }


    private long getLongPolicy(String policyCode) {
        return pointSystemPolicyRepository.findByPolicyCode(policyCode)
                .map(PointSystemPolicy::getPolicyValue)
                .map(Long::parseLong)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.POINT_POLICY_NOT_FOUND,
                                policyCode + " 정책이 존재하지 않습니다."
                        )
                );
    }

    private LocalDateTime calculateExpireAt(LocalDateTime now, PointItem item) {
        if (item.getExpireType() == ExpireType.FIXED_DATE) {
            if (item.getFixedExpireDt() == null) {
                throw new BusinessException(
                        ErrorCode.POINT_POLICY_VIOLATION,
                        "FIXED_DATE 타입이지만 fixed_expire_dt가 설정되어 있지 않습니다."
                );
            }
            return item.getFixedExpireDt();
        }

        if (item.getExpireType() == ExpireType.RELATIVE_DAYS) {
            if (item.getExpireDays() == null || item.getExpireDays() <= 0) {
                throw new BusinessException(
                        ErrorCode.POINT_POLICY_VIOLATION,
                        "RELATIVE_DAYS 타입이지만 expire_days가 올바르게 설정되어 있지 않습니다."
                );
            }
            return now.plusDays(item.getExpireDays());
        }

        throw new BusinessException(
                ErrorCode.POINT_POLICY_VIOLATION,
                "지원하지 않는 만료 타입입니다: " + item.getExpireType()
        );
    }

    private String buildSaveDescription(PointItem item) {
        return item.getItemName();
    }

}