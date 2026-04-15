package com.example.ev_charging_system1.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CctvStatusDTO {
    private String station;
    private String plateNumber;
    private String status;
    private String imageUrl;
}
