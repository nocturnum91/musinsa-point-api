package com.musinsa.point.api.point.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class PointUseCancelResponse {

    private Long useNo;
    private Long pointKey;
    private Long canceledAmount;
    private Long balanceAfter;
    private String orderNo;
    private LocalDateTime occurredAt;

}