package com.musinsa.point.application.point;

import com.musinsa.point.api.point.dto.request.PointUseCancelRequest;
import com.musinsa.point.api.point.dto.request.PointUseRequest;
import com.musinsa.point.api.point.dto.response.PointUseCancelResponse;
import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.member.repository.MemberRepository;
import com.musinsa.point.domain.point.entity.PointHistory;
import com.musinsa.point.domain.point.entity.PointUse;
import com.musinsa.point.domain.point.model.HistoryType;
import com.musinsa.point.domain.point.model.PointUseStatus;
import com.musinsa.point.domain.point.repository.PointHistoryRepository;
import com.musinsa.point.domain.point.repository.PointUseRepository;
import com.musinsa.point.global.exception.BusinessException;
import com.musinsa.point.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PointServiceUseCancelTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointUseRepository pointUseRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.findByMemberId("test_user1")
                .orElseThrow(() -> new IllegalStateException("test_user1 not found"));
    }

    @Test
    @DisplayName("포인트 사용 후 전체 사용 취소에 성공한다")
    void useCancelFullSuccessTest() {
        // Given: 우선 100P 사용
        PointUseRequest useRequest = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-USE-CANCEL-FULL-1")
                .useAmount(100L)
                .build();

        var useResponse = pointService.usePoint(useRequest);
        Long useNo = useResponse.getUseNo();

        PointUseCancelRequest cancelRequest = new PointUseCancelRequestBuilder()
                .memberId("test_user1")
                .useNo(useNo)
                .cancelAmount(100L)
                .build();

        // When
        PointUseCancelResponse response = pointService.cancelUse(cancelRequest);

        // Then
        PointUse pointUse = pointUseRepository.findById(useNo)
                .orElseThrow();

        assertThat(pointUse.getStatus()).isEqualTo(PointUseStatus.CANCEL);
        assertThat(response.getUseNo()).isEqualTo(useNo);
        assertThat(response.getCanceledAmount()).isEqualTo(100L);

        // history 검증
        PointHistory history = pointHistoryRepository.findById(response.getPointKey())
                .orElseThrow();

        assertThat(history.getHistoryType()).isEqualTo(HistoryType.USE_CANCEL);
        assertThat(history.getAmount()).isEqualTo(100L);
        assertThat(history.getMember().getMemberNo()).isEqualTo(member.getMemberNo());
    }

    @Test
    @DisplayName("포인트 사용 후 일부 금액만 사용 취소하면 상태는 PARTIAL_CANCEL이 된다")
    void useCancelPartialSuccessTest() {
        // Given: 200P 사용
        PointUseRequest useRequest = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-USE-CANCEL-PARTIAL-1")
                .useAmount(200L)
                .build();

        var useResponse = pointService.usePoint(useRequest);
        Long useNo = useResponse.getUseNo();

        // 100P만 취소
        PointUseCancelRequest cancelRequest = new PointUseCancelRequestBuilder()
                .memberId("test_user1")
                .useNo(useNo)
                .cancelAmount(100L)
                .build();

        // When
        PointUseCancelResponse response = pointService.cancelUse(cancelRequest);

        // Then
        PointUse pointUse = pointUseRepository.findById(useNo)
                .orElseThrow();

        assertThat(pointUse.getStatus()).isEqualTo(PointUseStatus.PARTIAL_CANCEL);
        assertThat(response.getUseNo()).isEqualTo(useNo);
        assertThat(response.getCanceledAmount()).isEqualTo(100L);

        PointHistory history = pointHistoryRepository.findById(response.getPointKey())
                .orElseThrow();

        assertThat(history.getHistoryType()).isEqualTo(HistoryType.USE_CANCEL);
        assertThat(history.getAmount()).isEqualTo(100L);
    }

    @Test
    @DisplayName("다른 회원의 사용 건을 취소하려 하면 예외가 발생한다")
    void useCancelDifferentMemberFailTest() {
        // Given: test_user1로 사용 50P
        PointUseRequest useRequest = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-USE-CANCEL-DIFF-1")
                .useAmount(50L)
                .build();

        var useResponse = pointService.usePoint(useRequest);
        Long useNo = useResponse.getUseNo();

        // test_user2가 동일 useNo 취소 시도
        PointUseCancelRequest cancelRequest = new PointUseCancelRequestBuilder()
                .memberId("test_user2")
                .useNo(useNo)
                .cancelAmount(50L)
                .build();

        // When
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.cancelUse(cancelRequest)
        );

        // Then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_USE_NOT_FOUND);
        assertThat(ex.getMessage()).contains("해당 회원의 사용 건이 아닙니다.");
    }

    @Test
    @DisplayName("취소 가능 금액을 초과하는 사용 취소 요청 시 예외가 발생한다")
    void useCancelTooMuchAmountFailTest() {
        // Given: 100P 사용
        PointUseRequest useRequest = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-USE-CANCEL-TOO-MUCH-1")
                .useAmount(100L)
                .build();

        var useResponse = pointService.usePoint(useRequest);
        Long useNo = useResponse.getUseNo();

        // 취소 가능 금액(100)보다 큰 값으로 취소 요청
        PointUseCancelRequest cancelRequest = new PointUseCancelRequestBuilder()
                .memberId("test_user1")
                .useNo(useNo)
                .cancelAmount(999_999L)
                .build();

        // When
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.cancelUse(cancelRequest)
        );

        // Then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_USE_CANCEL_INVALID);
    }

    /**
     * 테스트용 빌더 – DTO private 필드를 리플렉션으로 설정
     */
    private static class PointUseCancelRequestBuilder {

        private final PointUseCancelRequest req = new PointUseCancelRequest();

        PointUseCancelRequestBuilder memberId(String val) {
            set(req, "memberId", val);
            return this;
        }

        PointUseCancelRequestBuilder useNo(Long val) {
            set(req, "useNo", val);
            return this;
        }

        PointUseCancelRequestBuilder cancelAmount(Long val) {
            set(req, "cancelAmount", val);
            return this;
        }

        PointUseCancelRequest build() {
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

    /**
     * 테스트용 빌더 – DTO private 필드를 리플렉션으로 설정
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