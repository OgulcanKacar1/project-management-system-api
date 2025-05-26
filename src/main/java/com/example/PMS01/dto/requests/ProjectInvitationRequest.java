package com.example.PMS01.dto.requests;

import com.example.PMS01.entities.ProjectInvitation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitationRequest {
    private Long projectId;
    private String userEmail;
    private ProjectInvitation.ProjectRole role;
}
