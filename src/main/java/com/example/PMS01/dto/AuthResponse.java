package com.example.PMS01.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private List<String> roles;

    public AuthResponse(String token, Long userId, String email, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.type = "Bearer";
    }
}
