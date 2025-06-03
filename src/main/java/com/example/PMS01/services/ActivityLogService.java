package com.example.PMS01.services;

import com.example.PMS01.entities.ActivityLog;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.Task;
import com.example.PMS01.repositories.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTaskCreation(Task task, String performedBy) {
        ActivityLog log = ActivityLog.builder()
                .action("TASK_CREATED")
                .details("\"" + task.getTitle() + "\" başlıklı görev oluşturuldu")
                .entityType("TASK")
                .entityId(task.getId())
                .performedBy(performedBy)
                .project(task.getProject())
                .build();

        activityLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTaskStatusChange(Task task, Task.TaskStatus oldStatus, Task.TaskStatus newStatus, String performedBy) {
        ActivityLog log = ActivityLog.builder()
                .action("TASK_STATUS_CHANGED")
                .details("\"" + task.getTitle() + "\" başlıklı görevin durumu " +
                        oldStatus + " -> " + newStatus + " olarak değiştirildi")
                .entityType("TASK")
                .entityId(task.getId())
                .performedBy(performedBy)
                .project(task.getProject())
                .build();

        activityLogRepository.save(log);
    }

    public void logTaskActivity(Task task, String activityType, String description, String performedBy) {
        ActivityLog activityLog = ActivityLog.builder()
                .action(activityType)
                .details(description)
                .entityType("TASK")
                .entityId(task.getId())
                .project(task.getProject())
                .performedBy(performedBy)
                .createdAt(LocalDateTime.now())
                .build();

        activityLogRepository.save(activityLog);
    }
}