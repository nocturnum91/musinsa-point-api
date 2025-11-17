package com.musinsa.point.api.point.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class PointSaveCancelResponse {

    private Long saveNo;
    private Long pointKey;       // history_no
    private Long canceledAmount; // 취소된 금액
    private Long balanceAfter;
    private LocalDateTime occurredAt;

}
