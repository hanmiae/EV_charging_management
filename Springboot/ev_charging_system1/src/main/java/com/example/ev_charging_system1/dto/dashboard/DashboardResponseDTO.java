package com.example.ev_charging_system1.dto.dashboard;

import  com.example.ev_charging_system1.dto.detection.ChargingStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DashboardResponseDTO.java 파일
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDTO {
    private DashboardSummaryDTO summary;
    private List<CctvStatusDTO> cctv;
    private List<DashboardEventDTO> events;
    private List<MachineDTO> machines;
    private ChartDTO charts;

    // 🚀 복잡한 전체 경로를 지우고 깔끔하게 수정하세요.
    private List<ChargingStatusDTO> chargingStatusList;
}