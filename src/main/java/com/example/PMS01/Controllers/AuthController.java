package com.example.PMS01.Controllers;

import com.example.PMS01.dto.requests.LoginRequest;
import com.example.PMS01.dto.requests.SignupRequest;
import com.example.PMS01.dto.responses.LoginResponse;
import com.example.PMS01.dto.responses.UserResponse;
import com.example.PMS01.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }


}
