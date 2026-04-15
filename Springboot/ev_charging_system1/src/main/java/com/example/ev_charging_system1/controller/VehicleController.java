package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.entity.Vehicle;
import com.example.ev_charging_system1.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/vehicles")

public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService){
        this.vehicleService = vehicleService;
    }

    // 차량 등록
    @PostMapping
    public Vehicle createVehicle(@RequestBody Vehicle vehicle){
        return vehicleService.saveVehicle(vehicle);
    }

    // 차량 조회
    @GetMapping
    public List<Vehicle> getVehicles(){
        return vehicleService.getAllVehicles();
    }

}