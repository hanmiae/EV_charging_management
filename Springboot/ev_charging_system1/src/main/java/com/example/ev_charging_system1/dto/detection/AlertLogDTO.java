package com.example.ev_charging_system1.dto.detection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class AlertLogDTO {
    private Long id;
    private String time;
    private String msg;
    private String type;
}