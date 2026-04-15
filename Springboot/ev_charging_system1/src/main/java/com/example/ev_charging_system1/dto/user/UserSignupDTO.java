package com.example.ev_charging_system1.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class UserSignupDTO {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$",
            message = "아이디는 영어와 숫자 4~12자리입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 4, message = "비밀번호는 최소 4자리 이상입니다.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    @Pattern(regexp = "^[가-힣]{2,10}$",
            message = "이름은 한글만 입력 가능합니다.")
    private String name;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(
            regexp = "^010-\\d{4}-\\d{4}$",
            message = "전화번호 형식은 010-0000-0000 입니다."
    )
    private String phone;

    @NotBlank(message = "동을 선택해주세요.")
    private String building;

    @NotBlank(message = "호수를 입력해주세요.")
    private String unit;

    @NotBlank(message = "차량번호를 입력해주세요.")
    @Pattern(
            regexp = "^\\d{2,3}[가-힣]\\d{4}$",
            message = "차량번호 형식이 올바르지 않습니다. 예: 12가3456"
    )
    private String plateNumber;

    @NotBlank(message = "차량종류를 선택해주세요.")
    private String vehicleType;
}