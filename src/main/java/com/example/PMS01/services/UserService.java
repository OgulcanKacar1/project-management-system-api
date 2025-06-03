package com.example.PMS01.services;

import com.example.PMS01.dto.requests.PasswordChangeRequest;
import com.example.PMS01.dto.requests.UserUpdateRequest;
import com.example.PMS01.dto.responses.UserResponse;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectUserRole;
import com.example.PMS01.entities.Role;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.ProjectUserRoleRepository;
import com.example.PMS01.repositories.RoleRepository;
import com.example.PMS01.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, ProjectRepository projectRepository, ProjectUserRoleRepository projectUserRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.projectRepository = projectRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
    }

    public UserResponse getCurrentUserProfile() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    public void changePassword(PasswordChangeRequest request) {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Mevcut şifrenin doğruluğunu kontrol et
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mevcut şifre yanlış");
        }

        // Yeni şifre ve onay şifresinin aynı olduğunu kontrol et
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Yeni şifre ve onay şifresi uyuşmuyor");
        }

        // Yeni şifreyi hashleyerek kaydet
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Eğer email değişiyorsa ve yeni email zaten kullanımdaysa hata fırlat
        if (!user.getEmail().equals(request.getEmail()) && existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanımda");
        }

        // Kullanıcı bilgilerini güncelle
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // Kullanıcıyı kaydet
        User updatedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(updatedUser.getId())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .email(updatedUser.getEmail())
                .build();
    }

    @Transactional
    public void deleteCurrentUserAccount() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<Project> ownedProjects = projectRepository.findAllByCreatedByEmail(email);
        projectRepository.deleteAll(ownedProjects);

        List<ProjectUserRole> userRoles = projectUserRoleRepository.findAllByUserEmail(email);
        projectUserRoleRepository.deleteAll(userRoles);

        userRepository.delete(user);
    }



    public String getCurrentUserEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("Giriş yapmalısınız");
        }

        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
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
