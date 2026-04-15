package com.example.ev_charging_system1.controller;

import com.example.ev_charging_system1.dto.user.LoginDTO;
import com.example.ev_charging_system1.dto.user.UserProfileDTO;
import com.example.ev_charging_system1.dto.user.UserResponseDTO;
import com.example.ev_charging_system1.dto.user.UserSignupDTO;
import com.example.ev_charging_system1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/signup")
    public UserResponseDTO signup(@Valid @RequestBody UserSignupDTO dto){
        return userService.signup(dto);
    }

    // 로그인
    @PostMapping("/login")
    public UserResponseDTO login(@Valid @RequestBody LoginDTO dto) {
        System.out.println("loginId: " + dto.getLoginId());
        System.out.println("password: " + dto.getPassword());
        return userService.login(dto);
    }

    @GetMapping("/profile")
    public UserProfileDTO getProfile(@RequestParam Long userPk) {
        return userService.getUserProfile(userPk);
    }
}