package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.dto.detection.DbUsageResponseDTO;
import com.example.ev_charging_system1.service.DbUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
@RequiredArgsConstructor
public class DbUsageController {

    private final DbUsageService dbUsageService;

    /**
     * DB 사용량 및 통계 데이터 반환
     */
    @GetMapping("/usage")
    public ResponseEntity<DbUsageResponseDTO> getDbUsage() {
        DbUsageResponseDTO response = dbUsageService.getActualDbStats();
        return ResponseEntity.ok(response);
    }
}