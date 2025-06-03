package com.example.PMS01.Controllers;

import com.example.PMS01.dto.TaskChatMessageDTO;
import com.example.PMS01.services.TaskChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskChatController {

    private final TaskChatService taskChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket üzerinden mesaj gönderme
    @MessageMapping("/task.chat")
    public void processMessage(@Payload TaskChatMessageDTO messageDTO) {
        TaskChatMessageDTO savedMessage = taskChatService.sendMessage(messageDTO);
        // WebSocket mesajı websocket tarafından gönderildiği için burada ayrıca göndermiyoruz
    }

    // REST API üzerinden mesaj gönderme
    @PostMapping("/api/tasks/{taskId}/chat")
    public ResponseEntity<TaskChatMessageDTO> sendMessage(
            @PathVariable Long taskId,
            @RequestBody TaskChatMessageDTO messageDTO) {
        messageDTO.setTaskId(taskId);
        TaskChatMessageDTO savedMessage = taskChatService.sendMessage(messageDTO);
        return ResponseEntity.ok(savedMessage);
    }

    // Görevin önceki mesajlarını getirme
    @GetMapping("/api/tasks/{taskId}/chat")
    public ResponseEntity<List<TaskChatMessageDTO>> getTaskMessages(@PathVariable Long taskId) {
        List<TaskChatMessageDTO> messages = taskChatService.getTaskMessages(taskId);
        return ResponseEntity.ok(messages);
    }
}