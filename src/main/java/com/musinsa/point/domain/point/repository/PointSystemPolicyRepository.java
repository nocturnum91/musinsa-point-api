package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.point.entity.PointSystemPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointSystemPolicyRepository extends JpaRepository<PointSystemPolicy, String> {

    Optional<PointSystemPolicy> findByPolicyCode(String policyCode);

}