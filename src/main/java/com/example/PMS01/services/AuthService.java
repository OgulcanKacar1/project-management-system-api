package com.example.PMS01.services;

import com.example.PMS01.dto.requests.LoginRequest;
import com.example.PMS01.dto.requests.SignupRequest;
import com.example.PMS01.dto.responses.LoginResponse;
import com.example.PMS01.dto.responses.UserResponse;
import com.example.PMS01.entities.User;
import com.example.PMS01.exceptions.GlobalExceptionHandler;
import com.example.PMS01.exceptions.UserAlreadyExistsException;
import com.example.PMS01.repositories.RoleRepository;
import com.example.PMS01.repositories.UserRepository;
import com.example.PMS01.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user);
                UserResponse userResponse = UserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .build();

                return LoginResponse.builder()
                        .token(token)
                        .user(userResponse)
                        .message("Giriş başarılı")
                        .build();
            } else {
                throw new RuntimeException("E-posta veya şifre hatalı.");
            }
        } else {
            throw new RuntimeException("Kullanıcı bulunamadı.");
        }
    }

    public UserResponse signup(SignupRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Bu e-posta adresi zaten kayıtlı.");
        }

        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        User savedUser = userService.saveOneUser(newUser);

        return UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .build();
    }
}

