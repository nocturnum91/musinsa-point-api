package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.model.PointType;
import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder(access = AccessLevel.PRIVATE)
    private PointSave(Member member,
                      PointItem pointItem,
                      PointEvent pointEvent,
                      PointType pointType,
                      Long amount,
                      Long availableAmount,
                      LocalDateTime expireAt,
                      String isManualYn) {
        this.member = member;
        this.pointItem = pointItem;
        this.pointEvent = pointEvent;
        this.pointType = pointType;
        this.amount = amount;
        this.availableAmount = availableAmount;
        this.expireAt = expireAt;
        this.isManualYn = isManualYn;
    }

    /**
     * 이벤트/정책 기반 포인트 적립 생성
     */
    public static PointSave createSave(Member member,
                                       PointItem pointItem,
                                       PointEvent pointEvent,
                                       PointType pointType,
                                       long amount,
                                       LocalDateTime expireAt,
                                       boolean manual) {
        return PointSave.builder()
                .member(member)
                .pointItem(pointItem)
                .pointEvent(pointEvent)
                .pointType(pointType)
                .amount(amount)
                .availableAmount(amount)
                .expireAt(expireAt)
                .isManualYn(manual ? "Y" : "N")
                .build();
    }

    /**
     * 사용 시 가용 포인트 차감
     */
    public void decreaseAvailableAmount(long usedAmount) {
        if (usedAmount <= 0) {
            return;
        }
        long next = this.availableAmount - usedAmount;
        if (next < 0) {
            throw new IllegalArgumentException("사용 금액이 가용 금액보다 클 수 없습니다.");
        }
        this.availableAmount = next;
    }

    /**
     * 사용 취소 등으로 가용 포인트 증가
     */
    public void increaseAvailableAmount(long amount) {
        if (amount <= 0) {
            return;
        }
        this.availableAmount += amount;
    }

    public boolean isExpired(LocalDateTime now) {
        return expireAt.isBefore(now) || expireAt.isEqual(now);
    }

    public boolean isManual() {
        return "Y".equalsIgnoreCase(this.isManualYn);
    }

}
