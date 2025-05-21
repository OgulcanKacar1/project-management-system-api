package com.example.PMS01.services;

import com.example.PMS01.entities.Role;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.RoleRepository;
import com.example.PMS01.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveOneUser(User newUser) {
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        Role memberRole = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new RuntimeException("Role not found: MEMBER"));

        newUser.setRoles(Set.of(memberRole));
        return userRepository.save(newUser);
    }

    public User getOneUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User updateOneUser(Long userId, User newUser) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User foundUser = user.get();
            foundUser.setEmail(newUser.getUsername());
            if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
                foundUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }
            foundUser.setRoles(newUser.getRoles());
            foundUser.setFirstName(newUser.getFirstName());
            foundUser.setLastName(newUser.getLastName());
            foundUser.setActive(newUser.isActive());
            userRepository.save(foundUser);
            return foundUser;
        }else
            return null;
    }

    public void deleteOneUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }



    public void rehashAllUserPasswords() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Sadece düz metin şifreleri hash'le (BCrypt formatında olmayanlar)
            if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
                String hashedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
                System.out.println("Kullanıcı şifresi hashlendi: " + user.getEmail());
            }
    }
}
}
