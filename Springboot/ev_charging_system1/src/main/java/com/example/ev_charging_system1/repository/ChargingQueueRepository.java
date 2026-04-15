package com.example.ev_charging_system1.repository;

import com.example.ev_charging_system1.entity.ChargingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChargingQueueRepository extends JpaRepository<ChargingQueue, Long> {

    // 이미 대기중인지 확인
    boolean existsByVehicleIdAndStatus(Long vehicleId, String status);

    // 대기 목록 조회
    List<ChargingQueue> findByStatusOrderByRequestTimeAsc(String status);

    // 10분 지난 데이터 삭제
    @Modifying
    @Query("""
        DELETE FROM ChargingQueue cq
        WHERE cq.status = 'waiting'
        AND cq.requestTime <= :time
    """)
    void deleteExpiredQueue(@Param("time") LocalDateTime time);

    Optional<ChargingQueue> findTopByVehicleIdAndStatusOrderByRequestTimeDesc(
            Long vehicleId, String status
    );

}