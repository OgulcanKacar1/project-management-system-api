package com.example.PMS01.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private Long id;
    private String action;
    private String details;
    private String entityType;
    private Long entityId;
    private String performedBy;
    private LocalDateTime createdAt;
}