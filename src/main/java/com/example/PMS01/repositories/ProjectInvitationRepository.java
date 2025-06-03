package com.example.PMS01.repositories;

import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectInvitation;
import com.example.PMS01.entities.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long>{
    List<ProjectInvitation> findByInvitedUserAndStatus(User invitedUser, ProjectInvitation.InvitationStatus status);
    List<ProjectInvitation> findByProjectAndStatus(Project project, ProjectInvitation.InvitationStatus status);
    Optional<ProjectInvitation> findByProjectAndInvitedUserAndStatus(Project project, User invitedUser, ProjectInvitation.InvitationStatus status);
    boolean existsByProjectAndInvitedUserAndStatusNot(Project project, User invitedUser, ProjectInvitation.InvitationStatus status);
    List<ProjectInvitation> findByInvitedUser_EmailAndStatus(String email, ProjectInvitation.InvitationStatus status);
    List<ProjectInvitation> findByInvitedUser_Email(String email);
    boolean existsByProjectAndInvitedUserAndStatus(Project project, User invitedUser, ProjectInvitation.InvitationStatus status);
    List<ProjectInvitation> findByInvitedBy_Email(String invitedByEmail);
}
