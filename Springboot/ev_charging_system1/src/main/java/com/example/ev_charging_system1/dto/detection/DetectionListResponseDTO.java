package com.example.ev_charging_system1.dto.detection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class DetectionListResponseDTO {

    private List<ChargingStatusDTO> chargingStatusList;
    private List<AlertLogDTO> alertLogs;

}