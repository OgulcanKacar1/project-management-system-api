package com.example.PMS01.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "invited_user_id")
    private User invitedUser;

    @ManyToOne
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    public enum InvitationStatus {
        PENDING, ACCEPTED, REJECTED, EXPIRED
    }

    public enum ProjectRole {
        ADMIN, MEMBER
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = InvitationStatus.PENDING;
        }
    }

}
