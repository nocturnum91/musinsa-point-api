package com.musinsa.point.api.point.controller;

import com.musinsa.point.api.point.dto.request.PointHistoryRequest;
import com.musinsa.point.api.point.dto.request.PointSaveCancelRequest;
import com.musinsa.point.api.point.dto.request.PointSaveRequest;
import com.musinsa.point.api.point.dto.request.PointUseRequest;
import com.musinsa.point.api.point.dto.response.PointHistoryResponse;
import com.musinsa.point.api.point.dto.response.PointSaveCancelResponse;
import com.musinsa.point.api.point.dto.response.PointSaveResponse;
import com.musinsa.point.api.point.dto.response.PointUseResponse;
import com.musinsa.point.application.point.PointQueryService;
import com.musinsa.point.application.point.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointQueryService pointQueryService;
    private final PointService pointService;

    /**
     * 회원 포인트 이력 전체 조회
     */
    @PostMapping("/history")
    public List<PointHistoryResponse> getPointHistory(@RequestBody @Valid PointHistoryRequest request) {
        return pointQueryService.getMemberPointHistory(request.getMemberId())
                .stream()
                .map(PointHistoryResponse::from)
                .toList();
    }

    @PostMapping("/save")
    public PointSaveResponse savePoint(@RequestBody @Valid PointSaveRequest request) {
        return pointService.savePoint(request);
    }

    @PostMapping("/save-cancel")
    public PointSaveCancelResponse cancelSave(@RequestBody @Valid PointSaveCancelRequest request) {
        return pointService.cancelSave(request);
    }

    @PostMapping("/use")
    public PointUseResponse usePoint(@RequestBody @Valid PointUseRequest request) {
        return pointService.usePoint(request);
    }


}
