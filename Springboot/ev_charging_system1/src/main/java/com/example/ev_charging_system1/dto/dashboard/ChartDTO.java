package com.example.ev_charging_system1.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ChartDTO {

    // 시간대별 전력
    private List<String> powerLabels;
    private List<Integer> powerData;

    // 충전 상태
    private int charging;
    private int complete;
    private int waiting;

    // 위반
    private int normal;
    private int violation;

    // 점유율 (미니차트)
    private List<Integer> occupancy;
}