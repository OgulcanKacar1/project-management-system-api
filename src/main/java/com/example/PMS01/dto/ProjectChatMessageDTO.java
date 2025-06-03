package com.example.PMS01.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectChatMessageDTO {
    private Long id;
    private String content;
    private LocalDateTime sentAt;
    private Long senderId;
    private String senderName;
    private Long projectId;
    private String attachmentUrl;
}