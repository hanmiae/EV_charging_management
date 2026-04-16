package com.example.ev_charging_system1.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CHARGING_QUEUE")

public class ChargingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_pk")
    private Long queueId;

    @Column(name = "vehicle_pk")
    private Long vehicleId;

    @Column(name = "station_pk")
    private Long stationId;

    @Column(name = "request_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestTime;

    @Column(name = "status")
    private String status; // waiting / charging / cancelled

}
