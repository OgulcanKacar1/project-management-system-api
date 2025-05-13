package com.example.PMS01.services;

import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveOneUser(User newUser) {
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
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

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User login(String email, String password) {
        System.out.println("Giris yapiliyor: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + email));

        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("Şifre eşleşiyor mu: " + passwordMatches);
        System.out.println("Girilen şifre: " + password);
        System.out.println("DB'deki kodlanmış şifre: " + user.getPassword());

        if (passwordMatches) {
            return user;
        }

        return null;
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
