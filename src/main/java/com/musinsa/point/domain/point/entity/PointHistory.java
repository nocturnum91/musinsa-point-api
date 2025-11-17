package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.model.HistoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_history")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_no")
    private Long historyNo;  // == pointKey

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onPersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.occurredAt == null) {
            this.occurredAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    private PointHistory(Member member,
                         HistoryType historyType,
                         PointSave refSave,
                         PointUse refUse,
                         Long amount,
                         Long balanceAfter,
                         String description,
                         LocalDateTime occurredAt,
                         LocalDateTime createdAt) {
        this.member = member;
        this.historyType = historyType;
        this.refSave = refSave;
        this.refUse = refUse;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    /**
     * 적립(SAVE) 이력 생성
     */
    public static PointHistory createSaveHistory(Member member,
                                                 PointSave save,
                                                 long amount,
                                                 long balanceAfter,
                                                 LocalDateTime occurredAt,
                                                 String description) {
        return PointHistory.builder()
                .member(member)
                .historyType(HistoryType.SAVE)
                .refSave(save)
                .refUse(null)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .occurredAt(occurredAt)
                .createdAt(null) // @PrePersist에서 자동 세팅
                .build();
    }

    public static PointHistory createUseHistory(Member member,
                                                PointUse use,
                                                long amount,
                                                long balanceAfter,
                                                LocalDateTime occurredAt,
                                                String description) {
        return PointHistory.builder()
                .member(member)
                .historyType(HistoryType.USE)
                .refSave(null)
                .refUse(use)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .occurredAt(occurredAt)
                .build();
    }


}