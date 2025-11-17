package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.model.PointType;
import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_save")
public class PointSave extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "save_no")
    private Long saveNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no", nullable = false)
    private PointItem pointItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_no")
    private PointEvent pointEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 20)
    private PointType pointType;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "available_amount", nullable = false)
    private Long availableAmount;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @Column(name = "is_manual_yn", nullable = false, length = 1)
    private String isManualYn; // 'Y' / 'N'

}
