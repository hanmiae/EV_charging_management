package com.example.ev_charging_system1.repository;

import com.example.ev_charging_system1.entity.ChargingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChargingHistoryRepository extends JpaRepository<ChargingHistory, Long> {
    // 특정 사용자의 기록만 조회하는 쿼리 메서드
    List<ChargingHistory> findByUserPkOrderByStartTimeDesc(Long userPk);
}