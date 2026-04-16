package com.example.ev_charging_system1.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDTO {
    private String name;
    private String plateNumber;
    private String role;
}