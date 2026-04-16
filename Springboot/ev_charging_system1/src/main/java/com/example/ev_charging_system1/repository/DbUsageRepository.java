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
                    "  SELECT '사용자 데이터' as name, 250 as total, NVL(ROUND(SUM(bytes) / 1024 / 1024, 2), 0) as used " +
                    "  FROM user_segments " +
                    "  WHERE UPPER(segment_name) = 'USERS' " +

                    "  UNION ALL " +

                    "  SELECT '차량 등록 데이터' as name, 300 as total, NVL(ROUND(SUM(bytes) / 1024 / 1024, 2), 0) as used " +
                    "  FROM user_segments " +
                    "  WHERE UPPER(segment_name) = 'VEHICLES' " +

                    "  UNION ALL " +

                    "  SELECT '충전기 관리 데이터' as name, 150 as total, NVL(ROUND(SUM(bytes) / 1024 / 1024, 2), 0) as used " +
                    "  FROM user_segments " +
                    "  WHERE UPPER(segment_name) = 'CHARGING_STATION' " +

                    "  UNION ALL " +

                    "  SELECT '번호판 인식 로그' as name, 2500 as total, NVL(ROUND(SUM(bytes) / 1024 / 1024, 2), 0) as used " +
                    "  FROM user_segments " +
                    "  WHERE UPPER(segment_name) = 'DETECTION_LOG' " +

                    "  UNION ALL " +

                    "  SELECT '충전 이용 이력' as name, 1000 as total, NVL(ROUND(SUM(bytes) / 1024 / 1024, 2), 0) as used " +
                    "  FROM user_segments " +
                    "  WHERE UPPER(segment_name) = 'CHARGING_SESSION' " +

                    "  UNION ALL " +

                    "  SELECT '충전 대기열 데이터' as name, 100 as total, NVL(ROUND(SUM(bytes) / 1024 / 1024, 2), 0) as used " +
                    "  FROM user_segments " +
                    "  WHERE UPPER(segment_name) = 'CHARGING_QUEUE' " +
                    ")",
            nativeQuery = true)
    List<Object[]> getTableUsageNative();

    @Query(value =
            "SELECT " +
                    "COUNT(confidence) as total, " +
                    "NVL(ROUND(AVG(confidence) * 100, 1), 0) as accuracy " +
                    "FROM DETECTION_LOG " +
                    "WHERE confidence IS NOT NULL",
            nativeQuery = true)
    Map<String, Object> getDetectionStatsNative();
}