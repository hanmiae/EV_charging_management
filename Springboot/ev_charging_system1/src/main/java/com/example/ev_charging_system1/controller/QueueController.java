package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.dto.queue.QueueRequestDTO;
import com.example.ev_charging_system1.dto.queue.QueueResponseDTO;
import com.example.ev_charging_system1.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor

public class QueueController {

    private final QueueService queueService;

    // ✅ 대기 등록
    @PostMapping("/join")
    public String joinQueue(@RequestBody QueueRequestDTO dto) {
        queueService.registerQueue(dto);
        return "대기열 등록 완료";
    }

    // ✅ 대기 목록 조회
    @GetMapping("/waiting")
    public List<QueueResponseDTO> getWaitingList() {
        return queueService.getWaitingList();
    }

    // 취소
    @PostMapping("/cancel")
    public String cancelQueue(@RequestBody QueueRequestDTO dto) {
        queueService.cancelQueue(dto);
        return "대기열 취소 완료";
    }

    @GetMapping("/my")
    public boolean isMyQueue(@RequestParam Long userPk) {
        return queueService.isMyQueue(userPk);
    }
}