package com.musinsa.point.domain.point.repository;

import com.musinsa.point.domain.point.entity.PointEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointEventRepository extends JpaRepository<PointEvent, Long> {

    Optional<PointEvent> findByEventCodeAndUseYn(String eventCode, String useYn);

    // 노출 가능한 이벤트 조회
    // List<PointEvent> findByDisplayYnAndUseYnAndStartDtLessThanEqualAndEndDtIsNullOrEndDtGreaterThanEqual(
    //        String displayYn, String useYn, LocalDateTime now1, LocalDateTime now2);
}
