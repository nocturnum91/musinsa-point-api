package com.musinsa.point.domain.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_system_policy")
public class PointSystemPolicy {

    @Id
    @Column(name = "policy_code", length = 50)
    private String policyCode;

    @Column(name = "policy_value", nullable = false, length = 100)
    private String policyValue;

    @Column(name = "description", length = 255)
    private String description;

    public PointSystemPolicy(String policyCode, String policyValue, String description) {
        this.policyCode = policyCode;
        this.policyValue = policyValue;
        this.description = description;
    }

}
