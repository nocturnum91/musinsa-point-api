package com.musinsa.point.application.point;

import com.musinsa.point.api.point.dto.request.PointUseRequest;
import com.musinsa.point.api.point.dto.response.PointUseResponse;
import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.member.repository.MemberRepository;
import com.musinsa.point.domain.point.entity.PointItem;
import com.musinsa.point.domain.point.entity.PointSave;
import com.musinsa.point.domain.point.repository.PointHistoryRepository;
import com.musinsa.point.domain.point.repository.PointItemRepository;
import com.musinsa.point.domain.point.repository.PointSaveRepository;
import com.musinsa.point.global.exception.BusinessException;
import com.musinsa.point.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PointServiceUsePointTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointSaveRepository pointSaveRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointItemRepository pointItemRepository;

    @Test
    @DisplayName("포인트 사용에 성공하면 잔액이 차감되고 USE 이력이 생성된다")
    void usePoint_successTest() {
        // given
        Member member = memberRepository.findByMemberId("test_user1")
                .orElseThrow(() -> new IllegalStateException("테스트용 회원이 존재해야 합니다."));

        long beforeBalance = Optional.ofNullable(
                pointSaveRepository.sumAvailableAmountByMember(member)
        ).orElse(0L);

        PointUseRequest request = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-1001")
                .useAmount(100L)
                .build();

        long beforeHistoryCount = pointHistoryRepository.count();

        // when
        PointUseResponse response = pointService.usePoint(request);

        // then
        long afterBalance = Optional.ofNullable(
                pointSaveRepository.sumAvailableAmountByMember(member)
        ).orElse(0L);

        assertThat(afterBalance).isEqualTo(beforeBalance - 100L);

        // USE 이력이 1건 이상 늘어났는지만 간단히 검증
        long afterHistoryCount = pointHistoryRepository.count();
        assertThat(afterHistoryCount).isGreaterThan(beforeHistoryCount);

        assertThat(response.getUseNo()).isNotNull();
        assertThat(response.getUsedAmount()).isEqualTo(100L);
    }

    @Test
    @DisplayName("포인트 잔액이 부족하면 POINT_BALANCE_LACK 예외가 발생한다")
    void usePoint_balanceLackTest() {
        // given
        PointUseRequest request = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-9999")
                .useAmount(99999999L) // 충분히 큰 값으로 부족 상태 유도
                .build();

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.usePoint(request)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_BALANCE_LACK);
    }

    @Test
    @DisplayName("포인트는 수기 지급 → 만료 임박 → 적립 순으로 차감된다")
    void usePoint_priorityOrderTest() {
        // given
        Member member = memberRepository.findByMemberId("test_user1")
                .orElseThrow(() -> new IllegalStateException("테스트용 회원이 존재해야 합니다."));

        PointItem item = pointItemRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("테스트용 포인트 아이템이 존재해야 합니다."));

        LocalDateTime now = LocalDateTime.now();

        // 1) 수기 지급 버킷 (manual = true, expireAt = now + 30일)
        PointSave manualBucket = PointSave.createSave(
                member,
                item,
                item.getPointType(),     // PointType enum
                1000L,
                now.plusDays(30),
                true,                    // isManual = true
                "MANUAL_TEST"
        );
        pointSaveRepository.save(manualBucket);

        // 2) 만료 임박 버킷 (manual = false, expireAt = now + 1일)
        PointSave nearExpireBucket = PointSave.createSave(
                member,
                item,
                item.getPointType(),
                1000L,
                now.plusDays(1),
                false,
                "EVENT_NEAR_EXPIRE"
        );
        pointSaveRepository.save(nearExpireBucket);

        // 3) 만료 여유 버킷 (manual = false, expireAt = now + 10일)
        PointSave farExpireBucket = PointSave.createSave(
                member,
                item,
                item.getPointType(),
                1000L,
                now.plusDays(10),
                false,
                "EVENT_FAR_EXPIRE"
        );
        pointSaveRepository.save(farExpireBucket);

        // when: 총 1500P 사용 요청
        PointUseRequest request = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-PRIORITY-1")
                .useAmount(1500L)
                .build();

        pointService.usePoint(request);

        // then: 다시 조회해서 availableAmount 변화 확인
        PointSave manualAfter = pointSaveRepository.findById(manualBucket.getSaveNo())
                .orElseThrow();
        PointSave nearExpireAfter = pointSaveRepository.findById(nearExpireBucket.getSaveNo())
                .orElseThrow();
        PointSave farExpireAfter = pointSaveRepository.findById(farExpireBucket.getSaveNo())
                .orElseThrow();

        // 1) 수기 지급(Y) 버킷이 먼저 전액 차감되어야 함
        assertThat(manualAfter.getAvailableAmount()).isEqualTo(0L);

        // 2) 그 다음 만료 임박 버킷에서 나머지 500P 차감
        assertThat(nearExpireAfter.getAvailableAmount()).isEqualTo(500L);  // 1000 - 500

        // 3) 만료 여유 버킷은 사용되지 않아야 함
        assertThat(farExpireAfter.getAvailableAmount()).isEqualTo(1000L);
    }

    /**
     * 테스트용 빌더 – DTO 필드가 private 이라 리플렉션으로 세팅
     */
    private static class PointUseRequestBuilder {

        private final PointUseRequest req = new PointUseRequest();

        PointUseRequestBuilder memberId(String val) {
            set(req, "memberId", val);
            return this;
        }

        PointUseRequestBuilder orderNo(String val) {
            set(req, "orderNo", val);
            return this;
        }

        PointUseRequestBuilder useAmount(Long val) {
            set(req, "useAmount", val);
            return this;
        }

        PointUseRequest build() {
            return req;
        }

        private static void set(Object target, String fieldName, Object value) {
            try {
                var field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}