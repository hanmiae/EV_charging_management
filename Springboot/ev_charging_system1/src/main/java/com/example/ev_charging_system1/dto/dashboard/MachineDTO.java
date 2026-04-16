package com.example.ev_charging_system1.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class MachineDTO {
    private String stationNumber;
    private String status;
    private double usage;
}