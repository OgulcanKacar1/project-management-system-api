package com.example.PMS01.Controllers;

import com.example.PMS01.dto.requests.ProjectCreateRequest;
import com.example.PMS01.dto.responses.ProjectResponse;
import com.example.PMS01.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectCreateRequest request){
        ProjectResponse response = projectService.createProject(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-projects")
    public ResponseEntity<Map<String, List<ProjectResponse>>> getMyProjects() {
        Map<String, List<ProjectResponse>> projects = projectService.getMyProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
        ProjectResponse project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId,
                                                         @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse updated = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/leave/{projectId}")
    public ResponseEntity<?> leaveProject(@PathVariable Long projectId) {
        try {
            projectService.leaveProject(projectId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Projeden başarıyla çıkış yapıldı");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{projectId}/remove-member/{userEmail}")
    public ResponseEntity<?> removeMember(@PathVariable Long projectId, @PathVariable String userEmail) {
        try {
            projectService.removeMemberFromProject(projectId, userEmail);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Kullanıcı projeden çıkarıldı");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{projectId}/transfer-ownership/{newOwnerEmail}")
    public ResponseEntity<?> transferOwnership(@PathVariable Long projectId, @PathVariable String newOwnerEmail) {
        try {
            projectService.transferProjectAdmin(projectId, newOwnerEmail);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Proje sahipliği başarıyla transfer edildi");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

}
