package com.musinsa.point.api.point.dto.response;

import com.musinsa.point.domain.point.entity.PointHistory;
import com.musinsa.point.domain.point.entity.PointSave;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointSaveResponse {

    private Long saveNo;
    private Long pointKey;        // history_no (과제 예시 pointKey)
    private Long amount;          // 이번에 적립된 금액
    private Long balanceAfter;    // 적립 이후 총 잔액
    private LocalDateTime expireAt;
    private String description;   // 예: "회원 가입 축하 포인트 (event=SIGNUP_BONUS)"

    public static PointSaveResponse of(PointSave save, PointHistory history) {
        return PointSaveResponse.builder()
                .saveNo(save.getSaveNo())
                .pointKey(history.getHistoryNo())
                .amount(history.getAmount())
                .balanceAfter(history.getBalanceAfter())
                .expireAt(save.getExpireAt())
                .description(history.getDescription())
                .build();
    }

}