package com.example.PMS01.Controllers;

import com.example.PMS01.dto.RoleDTO;
import com.example.PMS01.dto.UserDTO;
import com.example.PMS01.entities.Role;
import com.example.PMS01.entities.User;
import com.example.PMS01.entities.UserRole;
import com.example.PMS01.exceptions.ResourceNotFoundException;
import com.example.PMS01.exceptions.UserAlreadyExistsException;
import com.example.PMS01.repositories.UserRepository;
import com.example.PMS01.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(userService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO newUserDTO) {
        User newUser = userService.convertToEntity(newUserDTO);
        User savedUser = userService.saveOneUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getOneUser(@PathVariable Long userId) {
        User user = userService.getOneUserById(userId);
        return ResponseEntity.ok(userService.convertToDTO(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserDTO updatedUserDTO) {
        User existingUser = userService.getOneUserById(userId);
        User updatedUser = userService.convertToEntity(updatedUserDTO);

        if (updatedUser.getPassword() == null || updatedUser.getPassword().isEmpty()) {
            updatedUser.setPassword(existingUser.getPassword());
        }

        User savedUser = userService.updateOneUser(userId, updatedUser);
        return ResponseEntity.ok(userService.convertToDTO(savedUser));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // MEMBER yerine ADMIN yetkisi olmalı
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteOneUser(userId);
            return ResponseEntity.ok("Kullanıcı başarıyla silindi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı silinemedi: " + e.getMessage());
        }
    }

//    @PatchMapping("/{userId}/deactivate")
//    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
//    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
//        User user = userService.deactivateUser(userId);
//        return ResponseEntity.ok(user);
//    }



    @GetMapping("/{id}/roles")
    public ResponseEntity<List<RoleDTO>> getUserRoles(@PathVariable Long id) {
        List<Role> roles = userService.getUserRoles(id);
        List<RoleDTO> roleDTOs = roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName()))
                .toList();
        return ResponseEntity.ok(roleDTOs);
    }


    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok("Rol başarıyla atandı.");
    }

//    @DeleteMapping("/{userId}/roles/{roleId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
//        userService.removeRoleFromUser(userId, roleId);
//        return ResponseEntity.ok("Rol başarıyla kaldırıldı.");
//    }
}