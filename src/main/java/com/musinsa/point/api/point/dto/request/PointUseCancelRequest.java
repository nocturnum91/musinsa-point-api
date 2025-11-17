package com.musinsa.point.api.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointUseCancelRequest {

    @NotBlank
    private String memberId;

    @NotNull
    private Long useNo;

    @NotNull
    private Long cancelAmount;   // 전체 or 부분 취소 금액

}