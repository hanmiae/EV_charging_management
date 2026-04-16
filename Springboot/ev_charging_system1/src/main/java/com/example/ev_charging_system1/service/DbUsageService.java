package com.example.ev_charging_system1.service;

import com.example.ev_charging_system1.dto.detection.DbUsageResponseDTO;
import com.example.ev_charging_system1.repository.DbUsageRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DbUsageService {

    private final DbUsageRepository dbUsageRepository;

    public DbUsageService(DbUsageRepository dbUsageRepository) {
        this.dbUsageRepository = dbUsageRepository;
    }

    public DbUsageResponseDTO getActualDbStats() {

        List<Object[]> rawList = dbUsageRepository.getTableUsageNative();
        List<DbUsageResponseDTO.TablespaceDTO> databaseList = new ArrayList<>();

        double totalCapacity = 0.0;

        for (Object[] row : rawList) {
            String name = row[0] != null ? row[0].toString() : "";
            double total = round2(toDouble(row[1]));
            double used = round2(toDouble(row[2]));
            double percent = round2(toDouble(row[3]));

            DbUsageResponseDTO.TablespaceDTO item =
                    new DbUsageResponseDTO.TablespaceDTO(name, total, used, percent);

            databaseList.add(item);
            totalCapacity += total;
        }

        Map<String, Object> stats = dbUsageRepository.getDetectionStatsNative();

        double accuracy = toDouble(stats.get("ACCURACY"));
        if (accuracy == 0.0) {
            accuracy = toDouble(stats.get("accuracy"));
        }

        double errorRate = 100.0 - accuracy;
        if (errorRate < 0) {
            errorRate = 0.0;
        }

        return DbUsageResponseDTO.builder()
                .accuracy(String.format("%.1f%%", round1(accuracy)))
                .errorRate(String.format("%.1f%%", round1(errorRate)))
                .totalCapacity(String.format("%.0f MB", totalCapacity))
                .dbStatus("정상")
                .databaseList(databaseList)
                .build();
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();

        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}