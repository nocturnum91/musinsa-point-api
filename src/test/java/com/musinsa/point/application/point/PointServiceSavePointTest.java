package com.musinsa.point.application.point;

import com.musinsa.point.api.point.dto.request.PointSaveRequest;
import com.musinsa.point.api.point.dto.response.PointSaveResponse;
import com.musinsa.point.global.exception.BusinessException;
import com.musinsa.point.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class PointServiceSavePointTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("기본 금액으로 포인트 적립에 성공한다")
    void savePoint_withDefaultAmount_successTest() {
        // given
        PointSaveRequest request = new PointSaveRequestBuilder()
                .memberId("test_user1")
                .itemNo(1L)    // data.sql의 회원가입 축하 아이템
                .amount(null)  // default_amount 사용
                .eventCode("SIGNUP_BONUS")
                .build();

        // when
        PointSaveResponse response = pointService.savePoint(request);

        // then
        assertThat(response.getSaveNo()).isNotNull();
        assertThat(response.getPointKey()).isNotNull();
        assertThat(response.getAmount()).isEqualTo(3000L); // default_amount=3000 가정
        assertThat(response.getBalanceAfter()).isGreaterThanOrEqualTo(3000L);
        assertThat(response.getExpireAt()).isNotNull();
        assertThat(response.getDescription()).contains("회원 가입"); // item_name 기반
    }

    @Test
    @DisplayName("존재하지 않는 회원의 포인트 적립 시 MEMBER_NOT_FOUND 예외가 발생한다")
    void savePoint_memberNotFoundTest() {
        // given
        PointSaveRequest request = new PointSaveRequestBuilder()
                .memberId("unknown_user")
                .itemNo(1L)
                .amount(1000L)
                .eventCode(null)
                .build();

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.savePoint(request)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("1회 최대 적립 가능 포인트를 초과하면 POINT_POLICY_VIOLATION 예외가 발생한다")
    void savePoint_exceedMaxSavePerRequestTest() {
        // given
        // data.sql: MAX_SAVE_PER_REQUEST = 10000 으로 설정되어 있다고 가정
        PointSaveRequest request = new PointSaveRequestBuilder()
                .memberId("test_user1")
                .itemNo(1L)
                .amount(20_000L) // 1회 최대 적립(10000) 초과
                .eventCode(null)
                .build();

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.savePoint(request)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_POLICY_VIOLATION);
    }

    @Test
    @DisplayName("회원 최대 보유 한도를 초과하면 POINT_BALANCE_EXCEEDED 예외가 발생한다")
    void savePoint_exceedMemberMaxBalanceTest() {
        // given
        // data.sql: member_no=2 의 max_balance=5000 으로 설정되어 있다고 가정
        // test_user2의 현재 잔액 + 4000 이 5000을 넘도록 시나리오 구성
        PointSaveRequest request = new PointSaveRequestBuilder()
                .memberId("test_user2")
                .itemNo(1L)
                .amount(6000L) // 단일 적립으로 한도 초과 유도
                .eventCode("ATTEND_DAILY")
                .build();

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> pointService.savePoint(request)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_BALANCE_EXCEEDED);
    }

    /**
     * 테스트용 빌더
     */
    private static class PointSaveRequestBuilder {

        private final PointSaveRequest request = new PointSaveRequest();

        PointSaveRequestBuilder memberId(String memberId) {
            setField(request, "memberId", memberId);
            return this;
        }

        PointSaveRequestBuilder itemNo(Long itemNo) {
            setField(request, "itemNo", itemNo);
            return this;
        }

        PointSaveRequestBuilder amount(Long amount) {
            setField(request, "amount", amount);
            return this;
        }

        PointSaveRequestBuilder eventCode(String eventCode) {
            setField(request, "eventCode", eventCode);
            return this;
        }

        PointSaveRequest build() {
            return request;
        }

        private static void setField(Object target, String fieldName, Object value) {
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