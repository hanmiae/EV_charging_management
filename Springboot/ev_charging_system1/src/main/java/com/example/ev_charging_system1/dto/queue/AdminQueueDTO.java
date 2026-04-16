package com.example.ev_charging_system1.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminQueueDTO {

    private String chargerId;         // A-01
    private Integer rank;             // 순번
    private String carNumber;         // 차량번호
    private Integer estimatedMinutes; // 예상시간
}