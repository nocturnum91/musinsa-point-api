package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.point.entity.PointUse;
import com.musinsa.point.domain.point.entity.PointUseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointUseDetailRepository extends JpaRepository<PointUseDetail, Long> {

    List<PointUseDetail> findByPointUse(PointUse pointUse);

    @Query("""
                SELECT COALESCE(SUM(d.usedAmount - d.canceledAmount), 0)
                FROM PointUseDetail d
                WHERE d.pointSave.saveNo = :saveNo
            """)
    Long getUsedAmount(@Param("saveNo") Long saveNo);

}