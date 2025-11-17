package com.musinsa.point.api.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointHistoryRequest {

    @NotBlank
    private String memberId;

}
