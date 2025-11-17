package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.point.entity.PointUse;
import com.musinsa.point.domain.point.entity.PointUseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointUseDetailRepository extends JpaRepository<PointUseDetail, Long> {

    List<PointUseDetail> findByPointUse(PointUse pointUse);

}