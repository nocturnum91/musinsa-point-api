package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.point.model.ExpireType;
import com.musinsa.point.domain.point.model.PointType;
import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_item")
public class PointItem extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_no")
    private Long itemNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 20)
    private PointType pointType;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "default_amount", nullable = false)
    private Long defaultAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "expire_type", nullable = false, length = 20)
    private ExpireType expireType;

    @Column(name = "fixed_expire_dt")
    private LocalDateTime fixedExpireDt;

    @Column(name = "expire_days")
    private Integer expireDays;

    @Column(name = "is_manual_yn", nullable = false, length = 1)
    private String isManualYn; // 'Y' or 'N'

    @Column(name = "description", length = 255)
    private String description;

}
