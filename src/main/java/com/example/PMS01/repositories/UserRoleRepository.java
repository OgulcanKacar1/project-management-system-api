package com.example.PMS01.repositories;

import com.example.PMS01.entities.Role;
import com.example.PMS01.entities.User;
import com.example.PMS01.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByRole(Role role);
    Optional<UserRole> findByUserAndRole(User user, Role role);
    boolean existsByUserAndRole(User user, Role role);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    int deleteUserRolesByUserId(Long userId);
}
