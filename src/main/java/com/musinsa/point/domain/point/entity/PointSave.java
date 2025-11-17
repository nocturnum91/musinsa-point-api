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
    private String isManualYn; // Y / N

    @Column(name = "event_code", length = 50)
    private String eventCode;  // FK 아님, 메타 정보

    @Builder(access = AccessLevel.PRIVATE)
    private PointSave(Member member,
                      PointItem pointItem,
                      PointType pointType,
                      Long amount,
                      Long availableAmount,
                      LocalDateTime expireAt,
                      String isManualYn,
                      String eventCode) {
        this.member = member;
        this.pointItem = pointItem;
        this.pointType = pointType;
        this.amount = amount;
        this.availableAmount = availableAmount;
        this.expireAt = expireAt;
        this.isManualYn = isManualYn;
        this.eventCode = eventCode;
    }

    public static PointSave createSave(Member member,
                                       PointItem pointItem,
                                       PointType pointType,
                                       long amount,
                                       LocalDateTime expireAt,
                                       boolean manual,
                                       String eventCode) {
        return PointSave.builder()
                .member(member)
                .pointItem(pointItem)
                .pointType(pointType)
                .amount(amount)
                .availableAmount(amount)
                .expireAt(expireAt)
                .isManualYn(manual ? "Y" : "N")
                .eventCode(eventCode)
                .build();
    }

    /**
     * 포인트 사용 시 차감
     */
    public void use(long useAmount) {
        if (useAmount <= 0) {
            throw new IllegalArgumentException("useAmount는 0보다 커야 합니다.");
        }
        if (this.availableAmount < useAmount) {
            throw new IllegalArgumentException("사용 가능 포인트를 초과했습니다.");
        }
        this.availableAmount -= useAmount;
    }

    /**
     * 사용/취소 롤백 시 복원
     */
    public void restore(long restoreAmount) {
        if (restoreAmount <= 0) {
            throw new IllegalArgumentException("restoreAmount는 0보다 커야 합니다.");
        }
        this.availableAmount += restoreAmount;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(this.expireAt);
    }

    public boolean isManual() {
        return "Y".equalsIgnoreCase(this.isManualYn);
    }

}
