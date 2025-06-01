package com.example.PMS01.Controllers;

import com.example.PMS01.dto.ActivityLogDTO;
import com.example.PMS01.entities.ActivityLog;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.ActivityLogRepository;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.ProjectUserRoleRepository;
import com.example.PMS01.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserRepository userRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ActivityLogDTO>> getProjectLogs(@PathVariable Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Kullanıcının projede üye olup olmadığını kontrol et
        boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, currentUser);
        if (!isMember) {
            throw new RuntimeException("Bu projeye erişim yetkiniz bulunmamaktadır");
        }

        List<ActivityLog> logs = activityLogRepository.findByProjectOrderByCreatedAtDesc(project);
        return ResponseEntity.ok(logs.stream().map(this::mapToDTO).collect(Collectors.toList()));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    private ActivityLogDTO mapToDTO(ActivityLog log) {
        return ActivityLogDTO.builder()
                .id(log.getId())
                .action(log.getAction())
                .details(log.getDetails())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}