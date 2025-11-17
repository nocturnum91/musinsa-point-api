package com.musinsa.point.api.point.dto.response;

import com.musinsa.point.domain.point.entity.PointHistory;
import com.musinsa.point.domain.point.model.HistoryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointHistoryResponse {

    private Long pointKey;               // history_no = 과제 예시 pointKey
    private HistoryType historyType;     // SAVE / USE / USE_CANCEL / EXPIRE …
    private Long amount;                 // + 적립 / - 사용
    private Long balanceAfter;           // 이 시점 기준 잔액
    private String description;          // 선택적 설명
    private LocalDateTime occurredAt;    // 발생 시각

    public static PointHistoryResponse from(PointHistory history) {
        return PointHistoryResponse.builder()
                .pointKey(history.getHistoryNo())
                .historyType(history.getHistoryType())
                .amount(history.getAmount())
                .balanceAfter(history.getBalanceAfter())
                .description(history.getDescription())
                .occurredAt(history.getOccurredAt())
                .build();
    }

}
