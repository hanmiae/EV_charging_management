package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.dto.queue.AdminQueueDTO;
import com.example.ev_charging_system1.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/queue")
@RequiredArgsConstructor

public class AdminQueueController {

    private final QueueService queueService;

    @GetMapping("/waiting")
    public List<AdminQueueDTO> getWaitingQueue() {
        return queueService.getWaitingQueueForAdmin();
    }
}