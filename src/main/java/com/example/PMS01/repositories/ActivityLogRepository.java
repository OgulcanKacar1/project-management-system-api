package com.example.PMS01.repositories;

import com.example.PMS01.entities.ActivityLog;
import com.example.PMS01.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByProject(Project project);
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<ActivityLog> findByProjectOrderByCreatedAtDesc(Project project);
}
