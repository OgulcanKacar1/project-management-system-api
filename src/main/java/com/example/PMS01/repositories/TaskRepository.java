package com.example.PMS01.repositories;

import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.Task;
import com.example.PMS01.entities.TaskStatus;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssigneesContaining(User assignee);
    List<Task> findByCreator(User creator);
    List<Task> findByDueDateBefore(LocalDateTime deadline);
    List<Task> findByStatus(TaskStatus status);

}
