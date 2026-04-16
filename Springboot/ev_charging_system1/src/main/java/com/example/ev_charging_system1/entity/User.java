package com.example.ev_charging_system1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pk")
    private Long userPk;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(name = "building")
    private String building;

    @Column(name = "unit")
    private String unit;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private String role = "USER";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 💡 클래스 중괄호 안쪽으로 들어왔습니다!
    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = "USER";
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}