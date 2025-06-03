package com.example.PMS01.repositories;

import com.example.PMS01.entities.TaskChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskChatRepository extends JpaRepository<TaskChatMessage, Long> {
    List<TaskChatMessage> findByTaskIdOrderBySentAtAsc(Long taskId);
}