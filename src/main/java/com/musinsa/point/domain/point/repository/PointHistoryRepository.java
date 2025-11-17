package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByMemberOrderByOccurredAtDesc(Member member);

}