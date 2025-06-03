package com.example.PMS01.services;

import com.example.PMS01.dto.ProjectChatMessageDTO;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectChatMessage;
import com.example.PMS01.entities.User;
import com.example.PMS01.repositories.ProjectChatMessageRepository;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectChatService {

    private final ProjectChatMessageRepository projectChatMessageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ProjectChatMessageDTO sendMessage(ProjectChatMessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Project project = projectRepository.findById(messageDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        ProjectChatMessage message = ProjectChatMessage.builder()
                .content(messageDTO.getContent())
                .sender(sender)
                .project(project)
                .attachmentUrl(messageDTO.getAttachmentUrl())
                .build();

        ProjectChatMessage savedMessage = projectChatMessageRepository.save(message);
        ProjectChatMessageDTO savedMessageDTO = convertToDTO(savedMessage);

        // WebSocket üzerinden mesajı gönder
        messagingTemplate.convertAndSend("/topic/project." + project.getId(), savedMessageDTO);

        return savedMessageDTO;
    }

    public List<ProjectChatMessageDTO> getProjectMessages(Long projectId) {
        List<ProjectChatMessage> messages = projectChatMessageRepository.findByProjectIdOrderBySentAt(projectId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProjectChatMessageDTO convertToDTO(ProjectChatMessage message) {
        return ProjectChatMessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .projectId(message.getProject().getId())
                .attachmentUrl(message.getAttachmentUrl())
                .build();
    }
}