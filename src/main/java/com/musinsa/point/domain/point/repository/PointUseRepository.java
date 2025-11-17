package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.point.entity.PointUse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointUseRepository extends JpaRepository<PointUse, Long> {

    List<PointUse> findByOrderNo(String orderNo);

}