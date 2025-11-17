package com.musinsa.point.api.point.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class PointUseResponse {

    private Long useNo;                 // point_use PK
    private Long pointKey;              // history_no (과제 예시 pointKey)
    private Long usedAmount;            // 총 사용 금액
    private Long balanceAfter;          // 사용 후 잔액
    private String orderNo;
    private LocalDateTime occurredAt;

}
