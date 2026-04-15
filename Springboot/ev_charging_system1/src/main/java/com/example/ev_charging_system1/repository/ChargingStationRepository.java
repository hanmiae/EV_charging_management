package com.example.ev_charging_system1.repository;

import com.example.ev_charging_system1.entity.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {

    Optional<ChargingStation> findByStationNumber(String stationNumber);
}