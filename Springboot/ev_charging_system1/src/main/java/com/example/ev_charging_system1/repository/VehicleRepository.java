package com.example.ev_charging_system1.repository;

import com.example.ev_charging_system1.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);

    Optional<Vehicle> findByUserPk(Long userPk); // 추가
}