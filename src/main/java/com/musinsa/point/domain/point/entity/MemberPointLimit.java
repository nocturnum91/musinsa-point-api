package com.musinsa.point.domain.point.entity;

import com.musinsa.point.domain.member.entity.Member;
import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "member_point_limit")
public class MemberPointLimit extends BaseDateTimeEntity {

    @Id
    @Column(name = "member_no")
    private Long memberNo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_no")
    private Member member;

    @Column(name = "max_balance", nullable = false)
    private Long maxBalance;

    public MemberPointLimit(Member member, Long maxBalance) {
        this.member = member;
        this.maxBalance = maxBalance;
    }

}
