package com.example.PMS01.Controllers;

import com.example.PMS01.dto.AuthResponse;
import com.example.PMS01.dto.LoginRequest;
import com.example.PMS01.entities.Role;
import com.example.PMS01.entities.User;
import com.example.PMS01.entities.UserRole;
import com.example.PMS01.exceptions.UserAlreadyExistsException;
import com.example.PMS01.security.JwtUtil;
import com.example.PMS01.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            String jwt = jwtUtil.generateToken(email);

            User user = userService.getOneUserByEmail(email);

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Hesabınız pasif durumdadır. Giriş yapamazsınız.");
            }

            List<String> roles = user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(Role::getName)
                    .collect(Collectors.toList());
            AuthResponse response = new AuthResponse(jwt, user.getId(), user.getEmail(), roles);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Hatalı email veya şifre");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Giriş yapılırken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        try {
            // E-posta kontrolü
            if (userService.existsByEmail(newUser.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Bu e-posta adresi zaten kayıtlı.");
            }

            User savedUser = userService.saveOneUser(newUser);

            String jwt = jwtUtil.generateToken(savedUser.getEmail());

            List<String> roles = savedUser.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(Role::getName)
                    .collect(Collectors.toList());

            AuthResponse response = new AuthResponse(jwt, savedUser.getId(), savedUser.getEmail(), roles);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kayıt olurken bir hata oluştu: " + e.getMessage());
        }
    }
}
