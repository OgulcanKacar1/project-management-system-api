package com.example.PMS01.Controllers;

import com.example.PMS01.dto.UserDTO;
import com.example.PMS01.dto.requests.LoginRequest;
import com.example.PMS01.dto.requests.PasswordChangeRequest;
import com.example.PMS01.dto.requests.SignupRequest;
import com.example.PMS01.dto.requests.UserUpdateRequest;
import com.example.PMS01.dto.responses.LoginResponse;
import com.example.PMS01.dto.responses.UserResponse;
import com.example.PMS01.entities.User;
import com.example.PMS01.security.JwtUtil;
import com.example.PMS01.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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


    @GetMapping("/{userId}")
    public User getOneUser(@PathVariable Long userId) {
        return userService.getOneUserById(userId);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteCurrentUserAccount();
        // Oturumu sonlandır
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{userId}")
    public User updateOneUser(@PathVariable Long userId, @RequestBody User newUser) {
        return userService.updateOneUser(userId, newUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        UserResponse userResponse = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody UserUpdateRequest request) {
        try {
            UserResponse updatedUser = userService.updateCurrentUserProfile(request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
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




}
