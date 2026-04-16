package com.example.ev_charging_system1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="VEHICLES")

public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_pk")
    private Long vehiclePk;

    @Column(name = "user_pk")
    private Long userPk;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
