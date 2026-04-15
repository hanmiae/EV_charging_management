package com.example.ev_charging_system1.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QueueRequestDTO {
    private Long userPk;
    private String chargerId; // A-01, B-01
}