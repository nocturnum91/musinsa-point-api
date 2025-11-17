package com.musinsa.point.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "내부 서버 오류가 발생했습니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "회원 정보를 찾을 수 없습니다."),

    // 포인트
    POINT_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_POLICY_404", "포인트 정책 정보를 찾을 수 없습니다."),
    POINT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_ITEM_404", "포인트 아이템 정보를 찾을 수 없습니다."),
    POINT_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_EVENT_404", "포인트 이벤트 정보를 찾을 수 없습니다."),
    POINT_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "POINT_POLICY_400", "포인트 정책을 위반한 요청입니다."),
    POINT_BALANCE_EXCEEDED(HttpStatus.BAD_REQUEST, "POINT_BALANCE_400", "보유 가능 포인트 한도를 초과합니다."),
    POINT_SAVE_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_SAVE_404", "포인트 적립 정보를 찾을 수 없습니다."),
    POINT_USE_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_USE_404", "포인트 사용 정보를 찾을 수 없습니다."),
    POINT_USE_CANCEL_INVALID(HttpStatus.BAD_REQUEST, "POINT_USE_CANCEL_400", "사용 취소가 불가능한 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
