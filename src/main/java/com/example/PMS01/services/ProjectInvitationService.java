package com.example.PMS01.services;

import com.example.PMS01.dto.requests.InvitationResponseRequest;
import com.example.PMS01.dto.requests.ProjectInvitationRequest;
import com.example.PMS01.dto.responses.ProjectInvitationResponse;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectInvitation;
import com.example.PMS01.entities.ProjectUserRole;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.ProjectInvitationRepository;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.ProjectUserRoleRepository;
import com.example.PMS01.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectInvitationService {
    private final ProjectInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserService userService;

    public ProjectInvitationService(ProjectInvitationRepository invitationRepository,
                                    UserRepository userRepository,
                                    ProjectRepository projectRepository,
                                    ProjectUserRoleRepository projectUserRoleRepository,
                                    UserService userService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.userService = userService;
    }

    @Transactional
    public ProjectInvitationResponse createInvitation(ProjectInvitationRequest request) {
        // Mevcut kullanıcıyı al
        String currentUserEmail = userService.getCurrentUserEmail();
        User inviter = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Oturum açmış kullanıcı bulunamadı"));

        // Projeyi kontrol et
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Proje sahibi veya admin yetkisini kontrol et
        boolean isAuthorized = project.getCreatedBy().equals(currentUserEmail) ||
                projectUserRoleRepository.existsByProjectAndUserAndRoleType(
                        project, inviter, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);

        if (!isAuthorized) {
            throw new RuntimeException("Bu projede davet gönderme yetkiniz bulunmuyor");
        }

        // Davet edilecek kullanıcıyı bul
        User invitedUser = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("Davet edilecek kullanıcı bulunamadı"));

        // Kullanıcı zaten projede mi kontrol et
        List<ProjectUserRole> existingRoles = projectUserRoleRepository.findByProjectAndUser(project, invitedUser);
        if (!existingRoles.isEmpty()) {
            throw new RuntimeException("Bu kullanıcı zaten projenin bir üyesi");
        }

        // Bekleyen davet var mı kontrol et
        if (invitationRepository.existsByProjectAndInvitedUserAndStatusNot(
                project, invitedUser, ProjectInvitation.InvitationStatus.REJECTED)) {
            throw new RuntimeException("Bu kullanıcı için zaten bekleyen bir davet bulunuyor");
        }

        // Yeni davet oluştur
        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .invitedUser(invitedUser)
                .invitedBy(inviter)
                .status(ProjectInvitation.InvitationStatus.PENDING)
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        ProjectInvitation savedInvitation = invitationRepository.save(invitation);

        return mapToResponse(savedInvitation);
    }

    public List<ProjectInvitationResponse> getPendingInvitationsForCurrentUser() {
        String email = userService.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<ProjectInvitation> invitations = invitationRepository.findByInvitedUserAndStatus(
                currentUser, ProjectInvitation.InvitationStatus.PENDING);

        return invitations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectInvitationResponse respondToInvitation(InvitationResponseRequest request) {
        String currentUserEmail = userService.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Oturum açmış kullanıcı bulunamadı"));

        ProjectInvitation invitation = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new RuntimeException("Davet bulunamadı"));

        if (!invitation.getInvitedUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Bu daveti yanıtlama yetkiniz yok");
        }

        if (invitation.getStatus() != ProjectInvitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Bu davet artık beklemede değil");
        }

        invitation.setRespondedAt(LocalDateTime.now());

        if (request.isAccept()) {
            invitation.setStatus(ProjectInvitation.InvitationStatus.ACCEPTED);

            // Kullanıcının bu rolde kaydı var mı kontrol et
            Project project = invitation.getProject();
            ProjectUserRole.ProjectRoleType roleType = mapToProjectUserRoleType(invitation.getRole());

            if (!projectUserRoleRepository.existsByProjectAndUserAndRoleType(project, currentUser, roleType)) {
                // Kullanıcıyı projeye ekle
                ProjectUserRole userRole = ProjectUserRole.builder()
                        .project(project)
                        .user(currentUser)
                        .roleType(roleType)
                        .build();

                projectUserRoleRepository.save(userRole);
            }
        } else {
            invitation.setStatus(ProjectInvitation.InvitationStatus.REJECTED);
        }

        return mapToResponse(invitationRepository.save(invitation));
    }

    private ProjectUserRole.ProjectRoleType mapToProjectUserRoleType(ProjectInvitation.ProjectRole role) {
        if (role == ProjectInvitation.ProjectRole.ADMIN) {
            return ProjectUserRole.ProjectRoleType.PROJECT_ADMIN;
        } else {
            return ProjectUserRole.ProjectRoleType.PROJECT_MEMBER;
        }
    }

    private ProjectInvitationResponse mapToResponse(ProjectInvitation invitation) {
        return ProjectInvitationResponse.builder()
                .id(invitation.getId())
                .projectId(invitation.getProject().getId())
                .projectName(invitation.getProject().getName())
                .invitedUserEmail(invitation.getInvitedUser().getEmail())
                .invitedByUserEmail(invitation.getInvitedBy().getEmail())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .respondedAt(invitation.getRespondedAt())
                .role(invitation.getRole())
                .build();
    }
}