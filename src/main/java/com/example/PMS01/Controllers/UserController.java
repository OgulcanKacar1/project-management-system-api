package com.example.PMS01.Controllers;

import com.example.PMS01.dto.UserDTO;
import com.example.PMS01.dto.requests.LoginRequest;
import com.example.PMS01.dto.requests.SignupRequest;
import com.example.PMS01.dto.responses.LoginResponse;
import com.example.PMS01.dto.responses.UserResponse;
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




}
