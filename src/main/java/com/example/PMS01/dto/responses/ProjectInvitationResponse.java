package com.example.PMS01.dto.responses;

import com.example.PMS01.entities.ProjectInvitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitationResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String invitedUserEmail;
    private String invitedByUserEmail;
    private ProjectInvitation.InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private ProjectInvitation.ProjectRole role;
}
