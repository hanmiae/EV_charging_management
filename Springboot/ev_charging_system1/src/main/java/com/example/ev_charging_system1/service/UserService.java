package com.example.ev_charging_system1.service;

import com.example.ev_charging_system1.dto.user.LoginDTO;
import com.example.ev_charging_system1.dto.user.UserProfileDTO;
import com.example.ev_charging_system1.dto.user.UserResponseDTO;
import com.example.ev_charging_system1.dto.user.UserSignupDTO;
import com.example.ev_charging_system1.entity.User;
import com.example.ev_charging_system1.entity.Vehicle;
import com.example.ev_charging_system1.repository.UserRepository;
import com.example.ev_charging_system1.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;

    // 회원가입
    public UserResponseDTO signup(UserSignupDTO dto) {

        // 1️⃣ 사용자 저장
        User user = new User();
        user.setLoginId(dto.getLoginId());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setBuilding(dto.getBuilding());
        user.setUnit(dto.getUnit());
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        // 2️⃣ 차량 저장
        Vehicle vehicle = new Vehicle();
        vehicle.setUserPk(savedUser.getUserPk());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setVehicleType(dto.getVehicleType());

        vehicleService.saveVehicle(vehicle);

        // 3️⃣ 응답 반환
        return new UserResponseDTO(
                savedUser.getUserPk(),
                savedUser.getLoginId(),
                savedUser.getName(),
                savedUser.getRole()
        );
    }

    // 로그인
    public UserResponseDTO login(LoginDTO dto) {

        User user = userRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() -> new RuntimeException("아이디가 존재하지 않습니다."));

        if (!user.getPassword().trim().equals(dto.getPassword().trim())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        return new UserResponseDTO(
                user.getUserPk(),
                user.getLoginId(),
                user.getName(),
                user.getRole()
        );
    }

    // 사용자 프로필 조회
    public UserProfileDTO getUserProfile(Long userPk) {

        User user = userRepository.findById(userPk)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Vehicle vehicle = vehicleRepository.findByUserPk(userPk)
                .orElseThrow(() -> new RuntimeException("차량 없음"));

        return new UserProfileDTO(
                user.getName(),
                vehicle.getPlateNumber(),
                user.getRole()
        );
    }
}