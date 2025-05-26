package com.example.PMS01.Controllers;

import com.example.PMS01.dto.requests.InvitationResponseRequest;
import com.example.PMS01.dto.requests.ProjectInvitationRequest;
import com.example.PMS01.dto.responses.ProjectInvitationResponse;
import com.example.PMS01.services.ProjectInvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-invitations")
public class ProjectInvitationController {
    private final ProjectInvitationService invitationService;

    public ProjectInvitationController(ProjectInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<?> createInvitation(@RequestBody ProjectInvitationRequest request) {
        try {
            ProjectInvitationResponse response = invitationService.createInvitation(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ProjectInvitationResponse>> getPendingInvitations() {
        return ResponseEntity.ok(invitationService.getPendingInvitationsForCurrentUser());
    }

    @PostMapping("/respond")
    public ResponseEntity<?> respondToInvitation(@RequestBody InvitationResponseRequest request) {
        try {
            ProjectInvitationResponse response = invitationService.respondToInvitation(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
