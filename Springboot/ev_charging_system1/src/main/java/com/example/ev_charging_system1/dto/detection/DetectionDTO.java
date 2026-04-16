package com.example.ev_charging_system1.dto.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // 🚀 Getter, Setter를 자동으로 만들어줍니다. (이게 있어야 오류가 안 남!)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionDTO {
    private String plateNumber;
    private Boolean isEv;        // 🚀 Boolean 타입은 getIsEv() 혹은 isEv()로 생성됨
    private Long stationPk;      // 🚀 getStationPk() 생성됨
    private Double confidence;
    private String imageUrl;
    private String plateImageUrl;
}