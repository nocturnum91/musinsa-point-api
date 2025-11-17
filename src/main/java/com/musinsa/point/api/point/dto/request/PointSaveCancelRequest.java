package com.musinsa.point.api.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointSaveCancelRequest {

    @NotBlank
    private String memberId;

    @NotNull
    private Long saveNo; // 어떤 적립 건을 취소할지

}
