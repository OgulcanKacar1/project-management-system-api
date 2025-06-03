package com.example.PMS01.services;

import com.example.PMS01.dto.TaskChatMessageDTO;
import com.example.PMS01.entities.ActivityLog;
import com.example.PMS01.entities.Task;
import com.example.PMS01.entities.TaskChatMessage;
import com.example.PMS01.entities.User;
import com.example.PMS01.exceptions.ResourceNotFoundException;
import com.example.PMS01.exceptions.UnauthorizedException;
import com.example.PMS01.repositories.ActivityLogRepository;
import com.example.PMS01.repositories.TaskChatRepository;
import com.example.PMS01.repositories.TaskRepository;
import com.example.PMS01.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskChatService {

    private final TaskChatRepository taskChatRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public TaskChatMessageDTO sendMessage(TaskChatMessageDTO messageDTO) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(messageDTO.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Görev bulunamadı"));

        // Kullanıcının bu göreve erişim hakkı var mı kontrol et
        if (!canUserAccessTask(currentUser, task)) {
            throw new UnauthorizedException("Bu göreve mesaj gönderme yetkiniz yok");
        }

        TaskChatMessage message = TaskChatMessage.builder()
                .content(messageDTO.getContent())
                .task(task)
                .sender(currentUser)
                .attachmentUrl(messageDTO.getAttachmentUrl())
                .build();

        TaskChatMessage savedMessage = taskChatRepository.save(message);

        // Aktivite logunu kaydet
        logTaskChatActivity(currentUser, "TASK_MESSAGE",
                "Göreve mesaj gönderildi: " + task.getTitle(), task);

        // DTO'yu oluştur
        TaskChatMessageDTO responseDTO = convertToDTO(savedMessage);

        // WebSocket aracılığıyla abonelere ilet
        messagingTemplate.convertAndSend("/topic/task/" + task.getId(), responseDTO);

        return responseDTO;
    }

    public List<TaskChatMessageDTO> getTaskMessages(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Görev bulunamadı"));

        if (!canUserAccessTask(currentUser, task)) {
            throw new UnauthorizedException("Bu görevin mesajlarını görme yetkiniz yok");
        }

        return taskChatRepository.findByTaskIdOrderBySentAtAsc(taskId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean canUserAccessTask(User user, Task task) {
        // Görevin proje sahibi, proje admini, göreve atanan kişi veya proje üyesi mi?
        return task.getAssignedUsers().contains(user) ||
                task.getProject().isProjectAdmin(user) ||
                task.getProject().getMembers().contains(user);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("Kullanıcı bulunamadı"));
    }

    private TaskChatMessageDTO convertToDTO(TaskChatMessage message) {
        return TaskChatMessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getUsername())
                .taskId(message.getTask().getId())
                .attachmentUrl(message.getAttachmentUrl())
                .build();
    }

    private void logTaskChatActivity(User user, String action, String details, Task task) {
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .details(details)
                .entityType("TASK")
                .entityId(task.getId())
                .performedBy(user.getUsername())
                .project(task.getProject())
                .build();

        activityLogRepository.save(log);
    }
}