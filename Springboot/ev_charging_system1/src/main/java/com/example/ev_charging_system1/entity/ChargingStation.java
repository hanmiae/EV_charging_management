package com.example.ev_charging_system1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CHARGING_STATION")
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_pk")
    private Long stationPk;

    @Column(name = "station_number")
    private String stationNumber;

    @Column(name = "camera_pk")
    private String cameraPk;

    @Column(name = "building")
    private String building;

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status;
}