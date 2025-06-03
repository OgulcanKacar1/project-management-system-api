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
    List<ProjectUserRole> findByUserAndProject(User user, Project project);
    boolean existsByProjectAndUser(Project project, User user);
    boolean existsByProjectAndUserEmailAndRoleType(Project project, String userEmail, ProjectUserRole.ProjectRoleType roleType);

    List<ProjectUserRole> findByProjectAndUserAndRoleType(Project project, User currentUser, ProjectUserRole.ProjectRoleType projectRoleType);
}
