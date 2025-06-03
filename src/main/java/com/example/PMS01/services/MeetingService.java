package com.example.PMS01.services;

import com.example.PMS01.dto.MeetingDTO;
import com.example.PMS01.entities.ActivityLog;
import com.example.PMS01.entities.Meeting;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.User;
import com.example.PMS01.exceptions.ResourceNotFoundException;
import com.example.PMS01.exceptions.UnauthorizedException;
import com.example.PMS01.repositories.ActivityLogRepository;
import com.example.PMS01.repositories.MeetingRepository;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public MeetingDTO createMeeting(MeetingDTO meetingDTO) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(meetingDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı"));

        // Sadece proje adminleri toplantı oluşturabilir
        if (!project.isProjectAdmin(currentUser)) {
            throw new UnauthorizedException("Sadece proje yöneticileri toplantı oluşturabilir");
        }

        Set<User> participants = meetingDTO.getParticipantIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id)))
                .collect(Collectors.toSet());

        Meeting meeting = Meeting.builder()
                .title(meetingDTO.getTitle())
                .description(meetingDTO.getDescription())
                .link(meetingDTO.getLink())
                .startTime(meetingDTO.getStartTime())
                .endTime(meetingDTO.getEndTime())
                .project(project)
                .participants(participants)
                .build();

        Meeting savedMeeting = meetingRepository.save(meeting);

        // Toplantı oluşturma log kaydı
        logMeetingActivity(currentUser, "MEETING_CREATED",
                "Toplantı oluşturuldu: " + meeting.getTitle(), savedMeeting);

        return convertToDTO(savedMeeting);
    }

    public List<MeetingDTO> getProjectMeetings(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadı"));

        // Kullanıcı projeye dahil olmalı
        if (!isUserInProject(currentUser, project)) {
            throw new UnauthorizedException("Bu projeye erişim izniniz yok");
        }

        List<Meeting> meetings = meetingRepository.findByProjectId(projectId);
        return meetings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<MeetingDTO> getUserMeetings() {
        User currentUser = getCurrentUser();
        List<Meeting> meetings = meetingRepository.findByParticipantsContaining(currentUser);
        return meetings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<MeetingDTO> getUpcomingMeetings() {
        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> meetings = meetingRepository.findByParticipantsContainingAndStartTimeAfter(currentUser, now);
        return meetings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public MeetingDTO updateMeeting(Long meetingId, MeetingDTO meetingDTO) {
        User currentUser = getCurrentUser();
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı"));

        // Sadece proje admini toplantıyı güncelleyebilir
        if (!meeting.getProject().isProjectAdmin(currentUser)) {
            throw new UnauthorizedException("Sadece proje yöneticisi toplantıyı güncelleyebilir");
        }

        Set<User> participants = meetingDTO.getParticipantIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id)))
                .collect(Collectors.toSet());

        meeting.setTitle(meetingDTO.getTitle());
        meeting.setDescription(meetingDTO.getDescription());
        meeting.setLink(meetingDTO.getLink());
        meeting.setStartTime(meetingDTO.getStartTime());
        meeting.setEndTime(meetingDTO.getEndTime());
        meeting.setParticipants(participants);

        Meeting updatedMeeting = meetingRepository.save(meeting);

        // Toplantı güncelleme log kaydı
        logMeetingActivity(currentUser, "MEETING_UPDATED",
                "Toplantı güncellendi: " + meeting.getTitle(), updatedMeeting);

        return convertToDTO(updatedMeeting);
    }

    @Transactional
    public void deleteMeeting(Long meetingId) {
        User currentUser = getCurrentUser();
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı"));

        // Sadece proje admini toplantıyı silebilir
        if (!meeting.getProject().isProjectAdmin(currentUser)) {
            throw new UnauthorizedException("Sadece proje yöneticisi toplantıyı silebilir");
        }

        // Toplantı silme log kaydı
        logMeetingActivity(currentUser, "MEETING_DELETED",
                "Toplantı silindi: " + meeting.getTitle(), meeting);

        meetingRepository.delete(meeting);
    }

    private void logMeetingActivity(User user, String action, String details, Meeting meeting) {
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .details(details)
                .entityType("MEETING")
                .entityId(meeting.getId())
                .performedBy(user.getUsername())
                .project(meeting.getProject())
                .build();

        activityLogRepository.save(log);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)  // email alanını kullanıyoruz (findByUsername yerine)
                .orElseThrow(() -> new UnauthorizedException("Kullanıcı bulunamadı"));
    }

    private boolean isUserInProject(User user, Project project) {
        return project.getMembers().contains(user) || project.isProjectAdmin(user);
    }

    private MeetingDTO convertToDTO(Meeting meeting) {
        Set<Long> participantIds = meeting.getParticipants().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return MeetingDTO.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .link(meeting.getLink())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .projectId(meeting.getProject().getId())
                .participantIds(participantIds)
                .build();
    }

    public MeetingDTO getMeetingDetail(Long meetingId) {
        User currentUser = getCurrentUser();
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı"));

        // Kullanıcı ya toplantının katılımcısı olmalı ya da proje yöneticisi olmalı
        if (!meeting.getParticipants().contains(currentUser) &&
                !meeting.getProject().isProjectAdmin(currentUser)) {
            throw new UnauthorizedException("Bu toplantıya erişim izniniz yok");
        }

        return convertToDTO(meeting);
    }
}