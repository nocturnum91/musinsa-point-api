package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.domain.point.model.PointUseStatus;
import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_use")
public class PointUse extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "use_no")
    private Long useNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private Member member;

    @Column(name = "order_no", nullable = false, length = 100)
    private String orderNo;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PointUseStatus status;

    public static PointUse createUse(Member member, String orderNo, long amount) {
        PointUse use = new PointUse();
        use.member = member;
        use.orderNo = orderNo;
        use.usedAmount = amount;
        use.status = PointUseStatus.USED;
        return use;
    }

}
