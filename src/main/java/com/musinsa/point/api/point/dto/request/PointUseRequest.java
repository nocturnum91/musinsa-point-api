package com.musinsa.point.api.point.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointUseRequest {

    @NotBlank
    private String memberId;

    @NotBlank
    private String orderNo;

    @Min(1)
    private Long useAmount;

}
