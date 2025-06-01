package com.example.PMS01.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDTO {
    private Long id;
    private String title;
    private String description;
    private String link;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long projectId;
    private Set<Long> participantIds;
}

