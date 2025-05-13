package com.example.PMS01.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob
    private String description;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ProjectUserRole> userRoles = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    public void addProjectAdmin(User user) {
        ProjectUserRole role = ProjectUserRole.builder()
                .project(this)
                .user(user)
                .roleType(ProjectUserRole.ProjectRoleType.PROJECT_ADMIN)
                .build();
        userRoles.add(role);

        members.add(user);
    }

    public void addProjectMember(User user) {
        ProjectUserRole role = ProjectUserRole.builder()
                .project(this)
                .user(user)
                .roleType(ProjectUserRole.ProjectRoleType.PROJECT_MEMBER)
                .build();
        userRoles.add(role);

        members.add(user);
    }

    public boolean isProjectAdmin(User user) {
        return userRoles.stream()
                .anyMatch(r -> r.getUser().equals(user) &&
                        r.getRoleType() == ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);
    }

}
