package com.example.PMS01.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@JsonIgnoreProperties({"projectUserRoles"})
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "projectUserRoles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectUserRole> projectUserRoles = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectInvitation> projectInvitations = new ArrayList<>();


    public boolean hasRole(User user, ProjectUserRole.ProjectRoleType roleType) {
        return projectUserRoles.stream()
                .anyMatch(r -> r.getUser().getId().equals(user.getId()) &&
                        r.getRoleType() == roleType);
    }

    public boolean isProjectAdmin(User user) {
        return hasRole(user, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);
    }

    public Set<User> getMembers() {
        return projectUserRoles.stream()
                .map(ProjectUserRole::getUser)
                .collect(Collectors.toSet());
    }

    // Projeye rol ekleme helper metodu
    public void addUserRole(ProjectUserRole role) {
        projectUserRoles.add(role);
        role.setProject(this);
    }

    // Projeden rol silme helper metodu
    public void removeUserRole(ProjectUserRole role) {
        projectUserRoles.remove(role);
        role.setProject(null);
    }

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectUserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActivityLog> activityLogs = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectInvitation> invitations = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Meeting> meetings = new HashSet<>();
}