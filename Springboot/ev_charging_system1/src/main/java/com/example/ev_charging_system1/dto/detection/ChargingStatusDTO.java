package com.example.ev_charging_system1.dto.detection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChargingStatusDTO {
        private String station;
        private String plate;
        private String isEV;
        private String entryTime;
        private String status;        // "정상" 또는 "비정상"이 담김
        private int chargeTime;
        private String imageUrl;
        private String plateImageUrl;
        private boolean warning;
        private String warningMsg;    // 🚀 추가: "일반 차량" 또는 "위반 차량" 텍스트가 담김
}