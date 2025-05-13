package com.example.PMS01.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_user_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRoleType roleType;

    public enum ProjectRoleType {
        PROJECT_ADMIN,
        PROJECT_MEMBER
    }

}
