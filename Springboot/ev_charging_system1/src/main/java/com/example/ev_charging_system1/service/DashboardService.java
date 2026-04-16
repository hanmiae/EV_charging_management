package com.example.ev_charging_system1.service;

import com.example.ev_charging_system1.dto.dashboard.*;
import com.example.ev_charging_system1.dto.detection.ChargingStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private DetectionLogService detectionService;

    /**
     * 메인 대시보드 데이터 통합 조회
     */
    public DashboardResponseDTO getDashboard() {
        DashboardResponseDTO dto = new DashboardResponseDTO();

        // 1. 실시간 충전 구역 현황 데이터 가져오기
        List<ChargingStatusDTO> chargingStatusList = new ArrayList<>();
        chargingStatusList.addAll(detectionService.getChargingStatusList("A동"));
        chargingStatusList.addAll(detectionService.getChargingStatusList("B동"));

        // 2. 전체 데이터 세팅
        dto.setSummary(getSummary());
        // 🚀 실시간 데이터를 기반으로 CCTV 상태 생성
        dto.setCctv(generateCctvStatusList(chargingStatusList));
        dto.setEvents(getEventLogs());
        dto.setMachines(getMachines());
        dto.setCharts(getCharts());
        dto.setChargingStatusList(chargingStatusList);

        return dto;
    }

    /**
     * 🚀 [에러 해결 포인트] 컨트롤러에서 호출하는 메서드명을 유지합니다.
     */
    public List<CctvStatusDTO> getCctvStatus() {
        // 기본 호출 시 A동, B동 데이터를 합쳐서 상태를 반환하도록 수정
        List<ChargingStatusDTO> currentStatus = new ArrayList<>();
        currentStatus.addAll(detectionService.getChargingStatusList("A동"));
        currentStatus.addAll(detectionService.getChargingStatusList("B동"));

        return generateCctvStatusList(currentStatus);
    }

    /**
     * 실시간 데이터를 CCTV 배지용 DTO(정상/위반 클래스 매핑)로 변환
     */
    private List<CctvStatusDTO> generateCctvStatusList(List<ChargingStatusDTO> realTimeList) {
        return realTimeList.stream().map(data -> {
            String statusClass = "waiting"; // 기본값

            // 상태 텍스트에 따른 CSS 클래스 매핑
            if ("위반".equals(data.getStatus())) {
                statusClass = "danger";
            } else if ("정상".equals(data.getStatus())) {
                statusClass = "normal";
            } else if ("충전 중".equals(data.getStatus())) {
                statusClass = "charging";
            } else if ("장시간 주차".equals(data.getStatus())) {
                statusClass = "warning";
            }

            return new CctvStatusDTO(
                    data.getStation(),          // "A-01"
                    data.getPlate(),            // "63러 2314"
                    statusClass,                // Vue에서 사용할 클래스명
                    data.getPlateImageUrl()     // 번호판 이미지
            );
        }).toList();
    }

    public DashboardSummaryDTO getSummary() {
        return new DashboardSummaryDTO(100, 10, 5, 78);
    }

    private List<DashboardEventDTO> getEventLogs() {
        return List.of(
                new DashboardEventDTO("13:02:20", "미등록 차량 감지 (386마 1144)", "LIVE"),
                new DashboardEventDTO("13:05:01", "63러 2314 충전 시작", "INFO")
        );
    }

    private List<MachineDTO> getMachines() {
        return List.of(
                new MachineDTO("A-01", "charging", 42.5),
                new MachineDTO("A-02", "available", 0.0),
                new MachineDTO("B-01", "charging", 78.2),
                new MachineDTO("B-02", "error", 0.0)
        );
    }

    private ChartDTO getCharts() {
        List<String> labels = List.of("10", "11", "12", "13", "14", "15");
        List<Integer> data = List.of(45, 52, 48, 70, 65, 85);
        List<Integer> occupancy = List.of(30, 45, 35, 55, 40, 60);
        return new ChartDTO(labels, data, 6, 2, 1, 8, 2, occupancy);
    }
}