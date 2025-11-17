package com.musinsa.point.domain.point.entity;

import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_use_detail")
public class PointUseDetail extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_no")
    private Long detailNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_no", nullable = false)
    private PointUse pointUse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_no", nullable = false)
    private PointSave pointSave;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Column(name = "canceled_amount", nullable = false)
    private Long canceledAmount;

}
