package com.musinsa.point.domain.point.entity;

import com.musinsa.point.global.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_event")
public class PointEvent extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_no")
    private Long eventNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no", nullable = false)
    private PointItem pointItem;

    @Column(name = "event_code", nullable = false, unique = true, length = 50)
    private String eventCode;

    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;

    @Column(name = "display_yn", nullable = false, length = 1)
    private String displayYn; // 'Y' / 'N'

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn; // 'Y' / 'N'

    @Column(name = "start_dt", nullable = false)
    private LocalDateTime startDt;

    @Column(name = "end_dt")
    private LocalDateTime endDt;

    @Column(name = "description", length = 255)
    private String description;

}
