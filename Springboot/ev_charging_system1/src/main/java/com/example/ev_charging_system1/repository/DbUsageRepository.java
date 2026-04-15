package com.example.ev_charging_system1.repository;

import com.example.ev_charging_system1.entity.DetectionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DbUsageRepository extends JpaRepository<DetectionLog, Long> {

    @Query(value =
            "SELECT name, total, used, " +
                    "       CASE WHEN total = 0 THEN 0 ELSE ROUND((used / total) * 100, 2) END as percent " +
                    "FROM (" +
                    "  SELECT '사용자 데이터' as name, 250::numeric as total, " +
                    "         COALESCE(ROUND(pg_total_relation_size('public.users') / 1024.0 / 1024.0, 2), 0) as used " +
                    "  UNION ALL " +
                    "  SELECT '차량 등록 데이터' as name, 300::numeric as total, " +
                    "         COALESCE(ROUND(pg_total_relation_size('public.vehicles') / 1024.0 / 1024.0, 2), 0) as used " +
                    "  UNION ALL " +
                    "  SELECT '충전기 관리 데이터' as name, 150::numeric as total, " +
                    "         COALESCE(ROUND(pg_total_relation_size('public.charging_station') / 1024.0 / 1024.0, 2), 0) as used " +
                    "  UNION ALL " +
                    "  SELECT '번호판 인식 로그' as name, 2500::numeric as total, " +
                    "         COALESCE(ROUND(pg_total_relation_size('public.detection_log') / 1024.0 / 1024.0, 2), 0) as used " +
                    "  UNION ALL " +
                    "  SELECT '충전 이용 이력' as name, 1000::numeric as total, " +
                    "         COALESCE(ROUND(pg_total_relation_size('public.charging_history') / 1024.0 / 1024.0, 2), 0) as used " +
                    "  UNION ALL " +
                    "  SELECT '충전 대기열 데이터' as name, 100::numeric as total, " +
                    "         COALESCE(ROUND(pg_total_relation_size('public.charging_queue') / 1024.0 / 1024.0, 2), 0) as used " +
                    ") t",
            nativeQuery = true)
    List<Object[]> getTableUsageNative();

    @Query(value =
            "SELECT " +
                    "COUNT(confidence) as total, " +
                    "COALESCE(ROUND((AVG(confidence) * 100)::numeric, 1), 0) as accuracy " +
                    "FROM detection_log " +
                    "WHERE confidence IS NOT NULL",
            nativeQuery = true)
    Map<String, Object> getDetectionStatsNative();
}
