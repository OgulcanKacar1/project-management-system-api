package com.example.PMS01.Controllers;

import com.example.PMS01.dto.ProjectChatMessageDTO;
import com.example.PMS01.security.JwtUtil;
import com.example.PMS01.services.ProjectChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectChatController {

    private final ProjectChatService projectChatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    // WebSocket üzerinden mesaj gönderme
    @MessageMapping("/project.chat")
    public void processMessage(@Payload ProjectChatMessageDTO messageDTO) {
        ProjectChatMessageDTO savedMessage = projectChatService.sendMessage(messageDTO);
    }

    // REST API üzerinden mesaj gönderme
    @PostMapping("/api/projects/{projectId}/chat")
    public ResponseEntity<ProjectChatMessageDTO> sendMessage(
            @PathVariable Long projectId,
            @RequestBody ProjectChatMessageDTO messageDTO,
            @RequestHeader("Authorization") String token) {

        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(token);

        messageDTO.setProjectId(projectId);
        messageDTO.setSenderId(userId);

        ProjectChatMessageDTO savedMessage = projectChatService.sendMessage(messageDTO);
        return ResponseEntity.ok(savedMessage);
    }

    // Projenin önceki mesajlarını getirme
    @GetMapping("/api/projects/{projectId}/chat")
    public ResponseEntity<List<ProjectChatMessageDTO>> getProjectMessages(@PathVariable Long projectId) {
        List<ProjectChatMessageDTO> messages = projectChatService.getProjectMessages(projectId);
        return ResponseEntity.ok(messages);
    }
}