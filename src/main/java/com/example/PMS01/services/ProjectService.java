package com.example.PMS01.services;

import com.example.PMS01.dto.MemberDTO;
import com.example.PMS01.dto.ProjectDTO;
import com.example.PMS01.dto.requests.ProjectCreateRequest;
import com.example.PMS01.dto.responses.ProjectResponse;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectUserRole;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(ProjectCreateRequest request){
        String username = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        // Önce projeyi kaydet
        Project savedProject = projectRepository.save(project);

        // Şimdi roller ekle
        ProjectUserRole adminRole = ProjectUserRole.builder()
                .project(savedProject)
                .user(currentUser)
                .roleType(ProjectUserRole.ProjectRoleType.PROJECT_ADMIN)
                .build();

        ProjectUserRole memberRole = ProjectUserRole.builder()
                .project(savedProject)
                .user(currentUser)
                .roleType(ProjectUserRole.ProjectRoleType.PROJECT_MEMBER)
                .build();

        // Projeye rolleri ekle
        savedProject.getProjectUserRoles().add(adminRole);
        savedProject.getProjectUserRoles().add(memberRole);

        // Güncellenmiş projeyi kaydet
        return convertToResponse(projectRepository.save(savedProject));
    }

    @Transactional(readOnly=true)
    public List<ProjectResponse> getMyProjects() {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        List<Project> projects = projectRepository.findAllByCreatedByEmailWithRoles(username);


        return projects.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ProjectResponse getProjectById(Long projectId) {
        String email = getCurrentUserEmail();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Kullanıcının projeye erişim hakkı var mı kontrol et
        boolean isMember = project.getProjectUserRoles().stream()
                .anyMatch(role -> role.getUser().getEmail().equals(email));

        if (!isMember) {
            throw new RuntimeException("Bu projeye erişim izniniz yok");
        }

        return convertToResponse(project);
    }

    public void deleteProject(Long projectId) {
        String email = getCurrentUserEmail();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        if (!project.getCreatedBy().getEmail().equals(email)) {
            throw new RuntimeException("Bu projeyi silmeye yetkiniz yok");
        }

        projectRepository.delete(project);
    }

    public ProjectResponse updateProject(Long projectId, ProjectCreateRequest request) {
        String email = getCurrentUserEmail();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        if (!project.getCreatedBy().getEmail().equals(email)) {
            throw new RuntimeException("Bu projeyi güncellemeye yetkiniz yok");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        projectRepository.save(project);
        return convertToResponse(project);
    }

    private String getCurrentUserEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("Giriş yapmalısınız");
        }

        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }





    public void addProjectUserRole(Project project, User user, ProjectUserRole.ProjectRoleType roleType) {
        if (!project.hasRole(user, roleType)) {
            ProjectUserRole role = ProjectUserRole.builder()
                    .project(project)
                    .user(user)
                    .roleType(roleType)
                    .build();
            project.getProjectUserRoles().add(role);
            user.getProjectRoles().add(role);
        }
    }

    private ProjectResponse convertToResponse(Project project) {
        List<MemberDTO> memberDTOs = project.getProjectUserRoles().stream()
                .collect(Collectors.groupingBy(
                        role -> role.getUser().getEmail(), // Email’e göre grupla
                        Collectors.mapping(
                                role -> role.getRoleType().name(), // Rol ismini al
                                Collectors.toList() // Liste halinde topla
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> MemberDTO.builder()
                        .email(entry.getKey())
                        .roles(entry.getValue())
                        .build())
                .toList();

//        System.out.println("memberDTOs: " + memberDTOs);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .createdBy(project.getCreatedBy().getEmail())
                .members(memberDTOs)
                .build();
    }


}
