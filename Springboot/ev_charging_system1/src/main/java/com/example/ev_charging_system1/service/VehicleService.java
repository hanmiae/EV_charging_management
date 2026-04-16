package com.example.ev_charging_system1.service;

import com.example.ev_charging_system1.entity.Vehicle;
import com.example.ev_charging_system1.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository){
        this.vehicleRepository = vehicleRepository;
    }

    // 차량 등록
    public Vehicle saveVehicle(Vehicle vehicle){
        return vehicleRepository.save(vehicle);
    }

    // 차량 전체 조회
    public List<Vehicle> getAllVehicles(){
        return vehicleRepository.findAll();
    }

}