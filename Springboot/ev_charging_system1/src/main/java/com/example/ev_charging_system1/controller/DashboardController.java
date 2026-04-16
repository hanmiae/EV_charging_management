package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.dto.dashboard.CctvStatusDTO;
import com.example.ev_charging_system1.dto.dashboard.DashboardResponseDTO;
import com.example.ev_charging_system1.dto.dashboard.DashboardSummaryDTO;
import com.example.ev_charging_system1.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor


public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("")
    public DashboardResponseDTO getDashboard() {
        return dashboardService.getDashboard();
    }

    // 🔹 요약
    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }

    // 🔹 CCTV
    @GetMapping("/cctv")
    public List<CctvStatusDTO> getCctv() {
        return dashboardService.getCctvStatus();
    }
}