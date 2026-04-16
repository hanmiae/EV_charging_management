package com.example.ev_charging_system1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="DETECTION_LOG")
public class DetectionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_pk")
    private Long logPk;

    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    @Column(name = "station_pk")
    private Long stationPk;

    @Column(name = "detection_time")
    private LocalDateTime detectionTime;

    @Column(name = "result", length = 20)
    private String result; // normal / violation

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "image_url", length = 255)
    private String imageUrl; // Parking View

    @Column(name = "plate_image_url", length = 255)
    private String plateImageUrl; // Plate View

    // 🚀 [추가!] 전기차 여부를 저장할 칸을 만들어요.
    @Column(name = "is_ev")
    private Boolean isEv;

    // 👉 자동 시간 세팅
    @PrePersist
    public void prePersist() {
        this.detectionTime = LocalDateTime.now();
    }
}