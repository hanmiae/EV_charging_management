package com.example.ev_charging_system1.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QueueResponseDTO {

    private Long queuePk;
    private String stationNumber;  // A-01
    private String plateNumber;    // 차량번호
    private LocalDateTime requestTime;
    private String status;
}