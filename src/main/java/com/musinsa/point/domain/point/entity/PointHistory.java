package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.model.HistoryType;
import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_history")
public class PointHistory extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_no")
    private Long historyNo;  // == pointKey 개념

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", nullable = false, length = 20)
    private HistoryType historyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_save_no")
    private PointSave refSave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_use_no")
    private PointUse refUse;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "balance_after")
    private Long balanceAfter;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

}