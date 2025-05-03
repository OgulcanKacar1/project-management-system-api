package com.example.PMS01.services;

import com.example.PMS01.dto.UserDTO;
import com.example.PMS01.entities.Role;
import com.example.PMS01.entities.User;
import com.example.PMS01.entities.UserRole;
import com.example.PMS01.exceptions.ResourceNotFoundException;
import com.example.PMS01.exceptions.UserAlreadyExistsException;
import com.example.PMS01.repositories.RoleRepository;
import com.example.PMS01.repositories.UserRepository;
import com.example.PMS01.repositories.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public User convertToEntity(UserDTO userDTO){
        return User.builder()
                .id(userDTO.getId())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .build();
    }

    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .build();
    }

    public List<User> getAllUsers() {
        logger.debug("Tüm kullanıcılar getiriliyor");
        List<User> users = userRepository.findAll();
        logger.debug("Kullanıcılar: {}", users);
        return users;
    }

    @Transactional
    public User saveOneUser(User newUser) {
        logger.info("Yeni kullanıcı oluşturuluyor: {}", newUser.getEmail());

        if (existsByEmail(newUser.getEmail())) {
            logger.warn("Kayıt başarısız: {} e-posta adresi zaten kayıtlı", newUser.getEmail());
            throw new UserAlreadyExistsException("Bu e-posta adresi zaten kayıtlı: " + newUser.getEmail());
        }

        try {
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            newUser.setActive(true);

            User savedUser = userRepository.save(newUser);

            Role memberRole = roleRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("Member rolü bulunamadı"));

            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(memberRole);
            userRoleRepository.save(userRole);

            logger.info("Kullanıcı başarıyla oluşturuldu: {}", savedUser.getEmail());
            return savedUser;
        } catch (Exception e) {
            logger.error("Kullanıcı kaydı sırasında hata: {}", e.getMessage());
            throw e;
        }
    }

    public User getOneUserById(Long userId) {
        logger.debug("ID'ye göre kullanıcı getiriliyor: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));
    }

    public User getOneUserByEmail(String email) {
        logger.debug("E-posta adresine göre kullanıcı getiriliyor: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }

    @Transactional
    public User updateOneUser(Long userId, User newUser) {
        logger.info("Kullanıcı güncelleniyor: {}", userId);

        try {
            User foundUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));

            if (!foundUser.getEmail().equals(newUser.getEmail()) && userRepository.existsByEmail(newUser.getEmail())) {
                throw new UserAlreadyExistsException("Bu e-posta adresi zaten kullanımda: " + newUser.getEmail());
            }

            foundUser.setFirstName(newUser.getFirstName());
            foundUser.setLastName(newUser.getLastName());
            foundUser.setEmail(newUser.getEmail());

            if (!newUser.getPassword().isBlank()) {
                foundUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }

            return userRepository.save(foundUser);
        } catch (Exception e) {
            logger.error("Kullanıcı güncellenirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteOneUser(Long userId) {
        logger.info("Kullanıcı siliniyor: {}", userId);

        try {
            // Önce veritabanından direkt silme işlemi yapalım
            // Bu şekilde Hibernate tarafından döngüsel referans sorunu önlenecek
            int deletedCount = userRoleRepository.deleteUserRolesByUserId(userId);
            logger.debug("Kullanıcı için {} adet rol ilişkisi silindi", deletedCount);

            // Silme işleminden önce kullanıcının varlığını kontrol et
            if (!userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId);
            }

            // Kullanıcıyı doğrudan ID üzerinden sil
            userRepository.deleteById(userId);
            logger.info("Kullanıcı başarıyla silindi: {}", userId);
        } catch (Exception e) {
            logger.error("Kullanıcı silme sırasında hata: {}", e.getMessage(), e);
            throw new RuntimeException("Kullanıcı silme işlemi başarısız oldu", e);
        }
    }


    @Transactional
    public User deactivateUser(Long userId) {
        logger.info("Kullanıcı pasifleştiriliyor: {}", userId);

        User user = getOneUserById(userId);
        user.setActive(false);
        User deactivatedUser = userRepository.save(user);
        logger.info("Kullanıcı başarıyla pasifleştirildi: {}", userId);
        return deactivatedUser;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    @Transactional
    public void rehashAllUserPasswords() {
        logger.info("Tüm kullanıcı şifreleri yeniden hashleniyor");

        try {
            List<User> users = userRepository.findAll();

            for (User user : users) {
                // Şifreyi yeniden hashle
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
            }

            logger.info("{} kullanıcının şifresi başarıyla yeniden hashlendi", users.size());
        } catch (Exception e) {
            logger.error("Şifre hashleme sırasında hata: {}", e.getMessage());
            throw e;
        }
    }

    public List<Role> getUserRoles(Long userId) {
        logger.debug("Kullanıcı rolleri getiriliyor: {}", userId);

        User user = getOneUserById(userId);
        List<UserRole> userRoles = userRoleRepository.findByUser(user);

        synchronized (userRoles) {
            return userRoles.stream()
                    .map(UserRole::getRole)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        logger.info("Kullanıcıya rol atanıyor - Kullanıcı: {}, Rol: {}", userId, roleId);

        try {
            User user = getOneUserById(userId);
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + roleId));

            if (userRoleRepository.existsByUserAndRole(user, role)) {
                logger.info("Rol zaten atanmış: Kullanıcı: {}, Rol: {}", userId, roleId);
                return;
            }

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);

            logger.info("Rol başarıyla atandı - Kullanıcı: {}, Rol: {}", userId, roleId);
        } catch (Exception e) {
            logger.error("Rol atama sırasında hata: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Kullanıcıdan rol kaldırır
     */
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        logger.info("Kullanıcıdan rol kaldırılıyor - Kullanıcı: {}, Rol: {}", userId, roleId);

        try {
            User user = getOneUserById(userId);
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + roleId));

            userRoleRepository.findByUserAndRole(user, role)
                    .ifPresentOrElse(
                            userRole -> {
                                userRoleRepository.delete(userRole);
                                logger.info("Rol başarıyla kaldırıldı - Kullanıcı: {}, Rol: {}", userId, roleId);
                            },
                            () -> {
                                logger.warn("Kullanıcının bu rolü yok - Kullanıcı: {}, Rol: {}", userId, roleId);
                            }
                    );
        } catch (Exception e) {
            logger.error("Rol kaldırma sırasında hata: {}", e.getMessage());
            throw e;
        }
    }
}