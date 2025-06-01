package com.example.PMS01.services;

import com.example.PMS01.dto.MemberDTO;
import com.example.PMS01.dto.ProjectDTO;
import com.example.PMS01.dto.requests.ProjectCreateRequest;
import com.example.PMS01.dto.responses.ProjectResponse;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectUserRole;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.ProjectUserRoleRepository;
import com.example.PMS01.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProjectUserRoleRepository projectUserRoleRepository;

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

    public Map<String, List<ProjectResponse>> getMyProjects() {
        String currentUserEmail = userService.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Kullanıcının oluşturduğu projeler
        List<Project> createdProjects = projectRepository.findByCreatedByEmail(currentUserEmail);
        List<ProjectResponse> createdProjectsResponse = createdProjects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Kullanıcının üye olduğu projeler
        List<ProjectUserRole> userRoles = projectUserRoleRepository.findByUser(currentUser);
        List<Project> memberProjects = userRoles.stream()
                .map(ProjectUserRole::getProject)
                .filter(p -> !p.getCreatedBy().getEmail().equals(currentUserEmail))
                .distinct() // Aynı projeyi birden fazla rolle çekmeyi önle
                .collect(Collectors.toList());

        List<ProjectResponse> memberProjectsResponse = memberProjects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Map<String, List<ProjectResponse>> result = new HashMap<>();
        result.put("ownedProjects", createdProjectsResponse);
        result.put("memberProjects", memberProjectsResponse);

        return result;
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
                        role -> role.getUser(),// Email’e göre grupla
                        Collectors.mapping(
                                role -> role.getRoleType().name(), // Rol ismini al
                                Collectors.toList() // Liste halinde topla
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> MemberDTO.builder()
                        .id(entry.getKey().getId())
                        .email(entry.getKey().getEmail())
                        .roles(entry.getValue())
                        .build())
                .toList();


        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .createdBy(project.getCreatedBy().getEmail())
                .members(memberDTOs)
                .build();
    }

    public void leaveProject(Long projectId) {
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        if (project.getCreatedBy().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Proje sahibi doğrudan projeden çıkamaz. Önce proje sahipliğini başka bir üyeye transfer etmelisiniz.");
        }

        List<ProjectUserRole> roles = projectUserRoleRepository.findByUserAndProject(currentUser, project);
        if (roles.isEmpty()) {
            throw new RuntimeException("Bu projede zaten üye değilsiniz");
        }

        projectUserRoleRepository.deleteAll(roles);
    }

    public void removeMemberFromProject(Long projectId, String memberEmail) {
        String currentUserEmail = getCurrentUserEmail();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        boolean isAdmin = projectUserRoleRepository.existsByProjectAndUserEmailAndRoleType(
                project, currentUserEmail, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);

        if (!project.getCreatedBy().getEmail().equals(currentUserEmail) && !isAdmin) {
            throw new RuntimeException("Bu işlemi yapma yetkiniz yok");
        }

        if (project.getCreatedBy().getEmail().equals(memberEmail)) {
            throw new RuntimeException("Proje sahibi projeden çıkarılamaz");
        }

        User memberToRemove = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("Çıkarılacak kullanıcı bulunamadı"));

        List<ProjectUserRole> roles = projectUserRoleRepository.findByUserAndProject(memberToRemove, project);
        if (roles.isEmpty()) {
            throw new RuntimeException("Bu kullanıcı zaten projede üye değil");
        }

        projectUserRoleRepository.deleteAll(roles);
    }

    @Transactional
    public void transferProjectAdmin(Long projectId, String newAdminEmail) {
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Oturum açmış kullanıcı bulunamadı"));

        // Yeni admin kullanıcısını bul
        User newAdmin = userRepository.findByEmail(newAdminEmail)
                .orElseThrow(() -> new RuntimeException("Yeni admin olarak atanacak kullanıcı bulunamadı"));

        // Projeyi kontrol et
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Mevcut kullanıcının proje admini olup olmadığını kontrol et
        boolean isCurrentUserAdmin = project.getCreatedBy().getEmail().equals(currentUserEmail) ||
                projectUserRoleRepository.existsByProjectAndUserAndRoleType(
                        project, currentUser, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);

        if (!isCurrentUserAdmin) {
            throw new RuntimeException("Bu işlem için admin yetkisine sahip değilsiniz");
        }

        // Yeni admin kullanıcısının projede olup olmadığını kontrol et
        if (!projectUserRoleRepository.existsByProjectAndUser(project, newAdmin)) {
            throw new RuntimeException("Transfer edilecek kullanıcı bu projenin üyesi değil");
        }

        // Proje sahipliğini transfer et
        project.setCreatedBy(newAdmin);

        // Yeni adminlik rolünü eklemeden önce, kullanıcının mevcut rollerini kontrol et
        boolean hasAdminRole = projectUserRoleRepository.existsByProjectAndUserAndRoleType(
                project, newAdmin, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);

        if (!hasAdminRole) {
            ProjectUserRole adminRole = ProjectUserRole.builder()
                    .project(project)
                    .user(newAdmin)
                    .roleType(ProjectUserRole.ProjectRoleType.PROJECT_ADMIN)
                    .build();

            projectUserRoleRepository.save(adminRole);
        }

        // Mevcut kullanıcıdan admin rolünü kaldır
        List<ProjectUserRole> currentUserAdminRoles = projectUserRoleRepository.findByProjectAndUserAndRoleType(
                project, currentUser, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);
        projectUserRoleRepository.deleteAll(currentUserAdminRoles);

        // Güncellenen projeyi kaydet
        projectRepository.save(project);
    }


}
