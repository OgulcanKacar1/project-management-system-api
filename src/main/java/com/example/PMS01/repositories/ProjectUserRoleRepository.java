package com.example.PMS01.repositories;

import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectUserRole;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRole, Long> {
    List<ProjectUserRole> findAllByUserEmail(String email);
    List<ProjectUserRole> findByProjectAndUser(Project project, User user);
    List<ProjectUserRole> findByUser(User user);
    boolean existsByProjectAndUserAndRoleType(Project project, User user, ProjectUserRole.ProjectRoleType roleType);
}
