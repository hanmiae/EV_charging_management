package com.example.ev_charging_system1.repository;

import com.example.ev_charging_system1.entity.DetectionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectionLogRepository extends JpaRepository<DetectionLog, Long> {

    // 이 메서드가 정확히 있어야 에러가 나지 않습니다!
    DetectionLog findTopByStationPkOrderByDetectionTimeDescLogPkDesc(Long stationPk);
}