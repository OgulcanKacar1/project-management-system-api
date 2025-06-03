package com.example.PMS01.repositories;

import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.Task;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    @Query("SELECT t FROM Task t JOIN t.assignedUsers u WHERE u = :user")
    List<Task> findByAssignedUserIn(User user);
    List<Task> findByProjectAndStatus(Project project, Task.TaskStatus status);
    List<Task> findByProjectOrderByDeadlineAsc(Project project);
}
