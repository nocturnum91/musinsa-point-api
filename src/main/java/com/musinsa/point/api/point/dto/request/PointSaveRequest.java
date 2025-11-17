package com.musinsa.point.api.point.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointSaveRequest {

    @NotBlank
    private String memberId;   // 어떤 회원에게

    @NotNull
    private Long itemNo;       // 어떤 포인트 아이템으로 적립할지

    @Min(1)
    private Long amount;       // null이면 item.defaultAmount 사용

    private String eventCode;  // 어떤 이벤트로 적립됐는지 (옵션, FK 아님)

}