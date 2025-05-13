package com.example.PMS01.Controllers;

import com.example.PMS01.dto.UserDTO;
import com.example.PMS01.entities.User;
import com.example.PMS01.security.JwtUtil;
import com.example.PMS01.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            if(userService.existsByEmail(userDTO.getEmail())){
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Bu e-posta adresi zaten kayıtlı.");
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(errorResponse);
            }
            User user = User.builder()
                    .firstName(userDTO.getFirstName())
                    .lastName(userDTO.getLastName())
                    .email(userDTO.getEmail())
                    .password(userDTO.getPassword())
                    .build();
            User savedUser = userService.saveOneUser(user);
            return ResponseEntity.ok(savedUser);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating user: " + e.getMessage()));
        }

    }
    @GetMapping("/{userId}")
    public User getOneUser(@PathVariable Long userId) {
        return userService.getOneUserById(userId);
    }
    @PutMapping("/{userId}")
    public User updateOneUser(@PathVariable Long userId, @RequestBody User newUser) {
        return userService.updateOneUser(userId, newUser);
    }

    @DeleteMapping ("/{userId}")
    public void deleteOneUser(@PathVariable Long userId) {
        userService.deleteOneUser(userId);
    }

    @PostMapping("/rehash-passwords")
    public ResponseEntity<String> rehashPasswords() {
        userService.rehashAllUserPasswords();
        return ResponseEntity.ok("Tüm kullanıcı şifreleri hashlenmiştir");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginRequest) {
        try {
            User user = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
            if (user != null) {
                String token = jwtUtil.generateToken(user.getEmail());

                // Hassas bilgileri filtreleyerek kullanıcı bilgilerini dön
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("email", user.getEmail());
                // Şifre gibi hassas bilgileri eklemiyoruz

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", userData);
                response.put("message", "Giriş başarılı");

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", userService.existsByEmail(loginRequest.getEmail()) ?
                        "E-posta veya şifre hatalı" : "Kullanıcı bulunamadı");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Giriş sırasında hata: " + e.getMessage()));
        }
    }


    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }




}
