package com.example.ev_charging_system1.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DashboardSummaryDTO {
    private int todayCharge;
    private int illegalCount;
    private int waitingCount;
    private int occupancyRate;
}