package com.example.PMS01.repositories;

import com.example.PMS01.entities.ProjectChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectChatMessageRepository extends JpaRepository<ProjectChatMessage, Long> {
    List<ProjectChatMessage> findByProjectIdOrderBySentAt(Long projectId);
}