package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.entity.ChargingHistory;
import com.example.ev_charging_system1.repository.ChargingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history") // 오타 확인!
@RequiredArgsConstructor
public class HistoryController {
    private final ChargingHistoryRepository historyRepository;

    @GetMapping("/my") // 오타 확인!
    public ResponseEntity<?> getMyHistory(@RequestParam Long userPk) {
        return ResponseEntity.ok(historyRepository.findByUserPkOrderByStartTimeDesc(userPk));
    }
}