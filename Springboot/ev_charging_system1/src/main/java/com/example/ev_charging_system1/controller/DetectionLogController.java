package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.dto.detection.DetectionDTO;
import com.example.ev_charging_system1.dto.detection.DetectionListResponseDTO;
import com.example.ev_charging_system1.entity.DetectionLog;
import com.example.ev_charging_system1.service.DetectionLogService;
import com.example.ev_charging_system1.service.PythonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/detection")
@RequiredArgsConstructor
public class DetectionLogController {

    private final DetectionLogService detectionLogService;
    private final PythonService pythonService;

    @GetMapping("/start")
    public ResponseEntity<String> startRecognition() {
        log.info("🔔 시스템 가동 신호 수신: 파이썬 엔진을 실행합니다.");
        try {
            pythonService.runIdentification();
            return ResponseEntity.ok("🚀 파이썬 엔진 가동 시작!");
        } catch (Exception e) {
            log.error("❌ 엔진 가동 실패: ", e);
            return ResponseEntity.status(500).body("❌ 엔진 가동 실패: " + e.getMessage());
        }
    }

    @PostMapping("/yolo/detect")
    public ResponseEntity<?> receiveDetection(@RequestBody DetectionDTO dto) {
        log.info("📡 파이썬 인식 결과 도착: {} (Station: {})", dto.getPlateNumber(), dto.getStationPk());
        try {
            DetectionLog logEntity = new DetectionLog();
            logEntity.setPlateNumber(dto.getPlateNumber());
            logEntity.setIsEv(dto.getIsEv());
            logEntity.setStationPk(dto.getStationPk());
            logEntity.setImageUrl(dto.getImageUrl());
            logEntity.setPlateImageUrl(dto.getPlateImageUrl());
            logEntity.setConfidence(dto.getConfidence());

            // 🚀 Result 설정은 Service의 saveLog 내부 determineVehicleStatus에서 처리하도록 맡깁니다.
            detectionLogService.saveLog(logEntity);
            return ResponseEntity.ok("✅ DB 저장 및 판별 완료");
        } catch (Exception e) {
            log.error("❌ 결과 수신 중 오류: ", e);
            return ResponseEntity.status(500).body("❌ 저장 실패: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        // 🚀 이 메서드가 서비스에서 '비정상' 등을 판별한 DTO 리스트를 반환합니다.
        return ResponseEntity.ok(detectionLogService.getDashboardData());
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts() {
        return ResponseEntity.ok(detectionLogService.getViolationLogs());
    }

    @GetMapping("/list")
    public DetectionListResponseDTO getList(@RequestParam String building) {
        return detectionLogService.getDetectionList(building);
    }
}