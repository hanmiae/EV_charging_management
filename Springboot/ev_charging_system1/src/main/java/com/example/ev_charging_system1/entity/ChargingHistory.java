package com.example.ev_charging_system1.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_pk")
    private Long historyPk;

    @Column(name = "user_pk") // DB 컬럼명과 정확히 매칭
    private Long userPk;

    @Column(name = "station_number")
    private String stationNumber;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String status;

    // 기본값을 0으로 설정하여 null 에러 방지
    @Column(name = "energy_used")
    private Double energyUsed = 0.0;

    @Column(name = "cost")
    private Integer cost = 0;
}