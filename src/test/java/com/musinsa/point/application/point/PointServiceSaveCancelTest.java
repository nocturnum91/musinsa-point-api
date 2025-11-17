package com.musinsa.point.application.point;

import com.musinsa.point.api.point.dto.request.PointSaveCancelRequest;
import com.musinsa.point.api.point.dto.request.PointUseRequest;
import com.musinsa.point.api.point.dto.response.PointSaveCancelResponse;
import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.member.repository.MemberRepository;
import com.musinsa.point.domain.point.entity.PointHistory;
import com.musinsa.point.domain.point.entity.PointSave;
import com.musinsa.point.domain.point.model.HistoryType;
import com.musinsa.point.domain.point.repository.PointHistoryRepository;
import com.musinsa.point.domain.point.repository.PointSaveRepository;
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
class PointServiceSaveCancelTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointSaveRepository pointSaveRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.findByMemberId("test_user1")
                .orElseThrow(() -> new IllegalStateException("test_user1 not found"));
    }

    @Test
    @DisplayName("적립 취소 성공 테스트")
    void saveCancelSuccessTest() {
        // Given
        // data.sql 기준: save_no = 1 이 test_user1의 적립 건이라고 가정
        Long targetSaveNo = 1L;

        PointSaveCancelRequest request = new PointSaveCancelRequestBuilder()
                .memberId("test_user1")
                .saveNo(targetSaveNo)
                .build();

        // When
        PointSaveCancelResponse response = pointService.cancelSave(request);

        // Then
        PointSave canceledSave = pointSaveRepository.findById(targetSaveNo)
                .orElseThrow();

        assertThat(canceledSave.getAvailableAmount()).isEqualTo(0L);
        assertThat(response.getSaveNo()).isEqualTo(targetSaveNo);
        assertThat(response.getCanceledAmount()).isEqualTo(canceledSave.getAmount());

        // history 검증
        PointHistory history = pointHistoryRepository.findById(response.getPointKey())
                .orElseThrow();

        assertThat(history.getHistoryType()).isEqualTo(HistoryType.SAVE_CANCEL);
        assertThat(history.getAmount()).isEqualTo(-canceledSave.getAmount());
        assertThat(history.getMember().getMemberNo()).isEqualTo(member.getMemberNo());
    }

    @Test
    @DisplayName("다른 회원의 적립 건을 취소하려 하면 예외 발생")
    void saveCancelDifferentMemberFailTest() {
        // Given
        // save_no = 1 은 test_user1의 것으로 가정
        PointSaveCancelRequest request = new PointSaveCancelRequestBuilder()
                .memberId("test_user2")  // 다른 회원
                .saveNo(1L)
                .build();

        // When
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.cancelSave(request)
        );

        // Then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_SAVE_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("해당 회원의 적립 건이 아닙니다.");
    }

    @Test
    @DisplayName("이미 일부 사용된 적립금은 취소할 수 없다")
    void saveCancelUsedFailTest() {

        // Given
        Long targetSaveNo = 2L;

        // 1) 우선 포인트가 충분하도록 /use 호출
        PointUseRequest useRequest = new PointUseRequestBuilder()
                .memberId("test_user1")
                .orderNo("ORDER-1001")
                .useAmount(100L) // 일부만 사용하도록 설정
                .build();

        pointService.usePoint(useRequest); // saveNo=1 일부 사용 발생

        // 2) 이제 같은 saveNo cancel 시도
        PointSaveCancelRequest cancelRequest = new PointSaveCancelRequestBuilder()
                .memberId("test_user1")
                .saveNo(targetSaveNo)
                .build();

        // When
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.cancelSave(cancelRequest)
        );

        // Then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_USE_CANCEL_INVALID);
    }

    /**
     * 테스트용 빌더 – DTO 필드가 private 이라 리플렉션으로 세팅
     */
    private static class PointSaveCancelRequestBuilder {

        private final PointSaveCancelRequest req = new PointSaveCancelRequest();

        PointSaveCancelRequestBuilder memberId(String val) {
            set(req, "memberId", val);
            return this;
        }

        PointSaveCancelRequestBuilder saveNo(Long val) {
            set(req, "saveNo", val);
            return this;
        }

        PointSaveCancelRequest build() {
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