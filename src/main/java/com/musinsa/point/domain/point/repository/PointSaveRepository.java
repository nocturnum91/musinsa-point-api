package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.entity.PointSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PointSaveRepository extends JpaRepository<PointSave, Long> {

    // 사용 가능한 포인트 조회 (수기 우선 → 만료 임박 → 적립 순 정렬)
    List<PointSave> findByMemberAndAvailableAmountGreaterThanAndExpireAtAfter(
            Member member, Long minAvailableAmount, LocalDateTime now);

    @Query("select coalesce(sum(s.availableAmount), 0) " +
            "from PointSave s " +
            "where s.member = :member")
    long sumAvailableAmountByMember(Member member);

    @Query("""
                SELECT ps
                FROM PointSave ps
                WHERE ps.member = :member
                  AND ps.availableAmount > 0
                ORDER BY ps.isManualYn DESC, ps.expireAt ASC, ps.createdAt ASC
            """)
    List<PointSave> findUsableBuckets(@Param("member") Member member);

}
