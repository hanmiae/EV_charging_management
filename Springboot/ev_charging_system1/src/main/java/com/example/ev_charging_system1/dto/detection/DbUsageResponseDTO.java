package com.example.ev_charging_system1.dto.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbUsageResponseDTO {
    private String accuracy;      // 인식 정확도 (예: "98.2%")
    private String errorRate;     // 인식 오차율 (예: "1.8%")
    private String totalCapacity; // 전체 용량 (예: "4300 MB")
    private String dbStatus;      // DB 상태 (예: "정상")

    private List<TablespaceDTO> databaseList; // 테이블별 사용량 리스트

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TablespaceDTO {
        private String name;    // 테이블명
        private Double total;   // 전체 (MB)
        private Double used;    // 사용 (MB)
        private Double percent; // 사용률 (%)
    }
}