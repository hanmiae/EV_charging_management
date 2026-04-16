package com.example.ev_charging_system1.service;

import com.example.ev_charging_system1.dto.detection.AlertLogDTO;
import com.example.ev_charging_system1.dto.detection.ChargingStatusDTO;
import com.example.ev_charging_system1.dto.detection.DetectionListResponseDTO;
import com.example.ev_charging_system1.entity.DetectionLog;
import com.example.ev_charging_system1.entity.Vehicle;
import com.example.ev_charging_system1.repository.DetectionLogRepository;
import com.example.ev_charging_system1.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DetectionLogService {

    private final DetectionLogRepository detectionLogRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * [전용 판별 메서드] 🚀 상태는 '비정상', 경고(warningMsg)는 상세 사유 입력
     */
    private void determineVehicleStatus(DetectionLog log, ChargingStatusDTO dto) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlateNumber(log.getPlateNumber());

        boolean isRegistered = vehicleOpt.isPresent();
        boolean isEv = false;

        if (isRegistered) {
            String type = vehicleOpt.get().getVehicleType();
            isEv = "EV".equalsIgnoreCase(type) || "전기차".equals(type);
        } else {
            isEv = log.getPlateNumber().replace(" ", "").contains("56나1425");
        }

        // 1. EV 여부 세팅
        dto.setIsEV(isEv ? "Yes" : "No");

        // 2. [상태 결정] 2번 사진(영상 대시보드)과 1번 사진(모니터링 표) 통합 로직
        if (isRegistered && isEv) {
            dto.setStatus("정상");
            dto.setWarning(false);
            dto.setWarningMsg("-");
        } else {
            // 비정상인 경우 (미등록이거나 내연기관차인 경우)
            dto.setStatus("비정상"); // 💡 표에는 '비정상'으로 표시
            dto.setWarning(true);

            if (!isEv) {
                dto.setWarningMsg("일반 차량"); // 💡 경고 컬럼에 표시될 내용
            } else {
                dto.setWarningMsg("위반 차량"); // 💡 경고 컬럼에 표시될 내용
            }
        }
    }

    public DetectionLog saveLog(DetectionLog log) {
        ChargingStatusDTO temp = new ChargingStatusDTO();
        determineVehicleStatus(log, temp);
        log.setResult("정상".equals(temp.getStatus()) ? "normal" : "violation");
        return detectionLogRepository.save(log);
    }

    public DetectionListResponseDTO getDetectionList(String building) {
        List<ChargingStatusDTO> chargingList = new ArrayList<>();
        List<AlertLogDTO> alertList = new ArrayList<>();
        Long[] stations = "A동".equals(building) ? new Long[]{1L, 2L} : new Long[]{3L, 4L};

        for (int i = 0; i < stations.length; i++) {
            Long stationPk = stations[i];
            DetectionLog log = detectionLogRepository.findTopByStationPkOrderByDetectionTimeDescLogPkDesc(stationPk);

            ChargingStatusDTO dto = new ChargingStatusDTO();
            dto.setStation(("A동".equals(building) ? "A" : "B") + "-0" + (i + 1));

            if (log != null) {
                dto.setPlate(log.getPlateNumber());
                dto.setEntryTime(log.getDetectionTime().toLocalTime().withNano(0).toString());
                dto.setImageUrl(log.getImageUrl());
                dto.setPlateImageUrl(log.getPlateImageUrl());
                dto.setChargeTime(10);

                // 판별 로직 호출
                determineVehicleStatus(log, dto);

                if (dto.isWarning()) {
                    AlertLogDTO alert = new AlertLogDTO();
                    alert.setId(log.getLogPk());
                    alert.setTime(dto.getEntryTime());
                    alert.setMsg("[" + dto.getStation() + "] " + dto.getWarningMsg() + " 감지!");
                    alert.setType("danger");
                    alertList.add(alert);
                }
            } else {
                dto.setPlate("-"); dto.setIsEV("-"); dto.setEntryTime("-"); dto.setStatus("-"); dto.setWarning(false);
            }
            chargingList.add(dto);
        }

        DetectionListResponseDTO response = new DetectionListResponseDTO();
        response.setChargingStatusList(chargingList);
        response.setAlertLogs(alertList);
        return response;
    }

    public List<ChargingStatusDTO> getChargingStatusList(String building) {
        return getDetectionList(building).getChargingStatusList();
    }

    public List<DetectionLog> getViolationLogs() {
        return detectionLogRepository.findAll().stream()
                .filter(log -> "violation".equals(log.getResult()))
                .toList();
    }

    /**
     * 🚀 [핵심 수정 부분] 영상 대시보드(2번 사진)용 데이터 생성 로직
     * 기존 DetectionLog 반환에서 ChargingStatusDTO 반환으로 변경하여 판별 로직을 적용함.
     */
    public List<ChargingStatusDTO> getDashboardData() {
        List<ChargingStatusDTO> result = new ArrayList<>();
        Long[] stations = {1L, 2L, 3L, 4L};

        for (Long stationPk : stations) {
            DetectionLog log = detectionLogRepository.findTopByStationPkOrderByDetectionTimeDescLogPkDesc(stationPk);
            ChargingStatusDTO dto = new ChargingStatusDTO();

            // 구역 이름 설정 (A-01, A-02, B-01, B-02)
            String zone = (stationPk <= 2 ? "A" : "B") + "-0" + (stationPk % 2 == 0 ? 2 : 1);
            dto.setStation(zone);

            if (log != null) {
                dto.setPlate(log.getPlateNumber());
                dto.setImageUrl(log.getImageUrl());
                // 💡 여기서 판별 로직을 호출해야 '비정상' 상태가 DTO에 주입됩니다.
                determineVehicleStatus(log, dto);
            } else {
                dto.setStatus("waiting"); // 로그 없으면 대기중
                dto.setPlate("-");
            }
            result.add(dto);
        }
        return result;
    }
}