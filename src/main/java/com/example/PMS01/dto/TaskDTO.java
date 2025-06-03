package com.example.PMS01.dto;

import com.example.PMS01.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deadline;
    private Long projectId;
    private String projectName;
    private Set<Long> assignedUserIds = new HashSet<>();
    private Set<String> assignedUserEmails = new HashSet<>();
    private String createdByEmail;
}