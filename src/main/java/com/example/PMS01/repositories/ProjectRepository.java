package com.example.PMS01.repositories;

import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedByEmail(String email);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.projectUserRoles pur LEFT JOIN FETCH pur.user WHERE p.createdBy.email = :email")
    List<Project> findAllByCreatedByEmailWithRoles(@Param("email") String email);
    List<Project> findAllByCreatedByEmail(String email);
}
