package com.musinsa.point.application.point;

import com.musinsa.point.api.point.dto.request.PointHistoryRequest;
import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.member.repository.MemberRepository;
import com.musinsa.point.domain.point.entity.PointHistory;
import com.musinsa.point.domain.point.repository.PointHistoryRepository;
import com.musinsa.point.global.exception.BusinessException;
import com.musinsa.point.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointQueryService {

    private final MemberRepository memberRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public List<PointHistory> getMemberPointHistory(String memberId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return pointHistoryRepository.findByMemberOrderByOccurredAtDesc(member);
    }

}
