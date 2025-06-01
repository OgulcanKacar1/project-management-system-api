package com.example.PMS01.Controllers;

import com.example.PMS01.dto.TaskDTO;
import com.example.PMS01.entities.Task;
import com.example.PMS01.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        List<TaskDTO> tasks = taskService.getTasksByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskDTO>> getCurrentUserTasks() {
        List<TaskDTO> tasks = taskService.getCurrentUserTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Task.TaskStatus newStatus) {
        TaskDTO updatedTask = taskService.updateTaskStatus(taskId, newStatus);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable Task.TaskStatus status) {
        List<TaskDTO> tasks = taskService.getTasksByStatus(projectId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}/by-deadline")
    public ResponseEntity<List<TaskDTO>> getTasksSortedByDeadline(@PathVariable Long projectId) {
        List<TaskDTO> tasks = taskService.getTasksSortedByDeadline(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long taskId) {
        TaskDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }
}
