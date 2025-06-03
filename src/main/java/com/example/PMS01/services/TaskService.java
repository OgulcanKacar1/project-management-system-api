package com.example.PMS01.services;

import com.example.PMS01.dto.TaskDTO;
import com.example.PMS01.entities.Project;
import com.example.PMS01.entities.ProjectUserRole;
import com.example.PMS01.entities.Task;
import com.example.PMS01.entities.User;
import com.example.PMS01.exceptions.ResourceNotFoundException;
import com.example.PMS01.exceptions.UnauthorizedException;
import com.example.PMS01.repositories.ProjectRepository;
import com.example.PMS01.repositories.ProjectUserRoleRepository;
import com.example.PMS01.repositories.TaskRepository;
import com.example.PMS01.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ActivityLogService activityLogService;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(taskDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        boolean isProjectAdmin = project.getCreatedBy().getEmail().equals(currentUser.getEmail()) ||
                projectUserRoleRepository.existsByProjectAndUserAndRoleType(
                        project, currentUser, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);

        if (!isProjectAdmin) {
            throw new RuntimeException("Görev oluşturmak için proje yöneticisi olmalısınız");
        }

        Set<User> assignedUsers = new HashSet<>();
        if (taskDTO.getAssignedUserIds() != null && !taskDTO.getAssignedUserIds().isEmpty()) {
            for (Long userId : taskDTO.getAssignedUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Atanacak kullanıcı bulunamadı: " + userId));

                // Kullanıcının projede üye olup olmadığını kontrol et
                boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, user);
                if (!isMember) {
                    throw new RuntimeException("Kullanıcı " + user.getEmail() + " bu projenin üyesi değil");
                }
                assignedUsers.add(user);
            }
        }

        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(Task.TaskStatus.TODO)
                .deadline(taskDTO.getDeadline())
                .project(project)
                .assignedUsers(assignedUsers)
                .createdBy(currentUser)
                .lastModifiedBy(currentUser.getEmail())
                .build();

        Task savedTask = taskRepository.save(task);
        activityLogService.logTaskCreation(savedTask,currentUser.getFirstName() + " " + currentUser.getLastName());

        return mapToDTO(savedTask);
    }

    // Projedeki tüm görevleri listeleme (sadece proje üyeleri için)
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProjectId(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Kullanıcının projede üye olup olmadığını kontrol et
        boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, currentUser);
        if (!isMember) {
            throw new RuntimeException("Bu projeye erişim yetkiniz bulunmamaktadır");
        }

        List<Task> tasks = taskRepository.findByProject(project);
        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Kullanıcıya atanan görevleri listeleme
    @Transactional(readOnly = true)
    public List<TaskDTO> getCurrentUserTasks() {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskRepository.findByAssignedUserIn(currentUser);
        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Görev durumu güncelleme (sadece görevin atandığı kişi yapabilir)
    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, Task.TaskStatus newStatus) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Görev bulunamadı"));

        // Yetki kontrolü - sadece görevin atandığı kişi veya proje yöneticisi durumu güncelleyebilir
        boolean isAssigned = task.getAssignedUsers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
        boolean isProjectAdmin = task.getProject().getCreatedBy().getId().equals(currentUser.getId()) ||
                projectUserRoleRepository.existsByProjectAndUserAndRoleType(
                        task.getProject(), currentUser, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);

        if (!isAssigned && !isProjectAdmin) {
            throw new RuntimeException("Bu görevi güncelleme yetkiniz bulunmamaktadır");
        }
        Task.TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        task.setLastModifiedBy(currentUser.getEmail());

        Task updatedTask = taskRepository.save(task);
        activityLogService.logTaskStatusChange(updatedTask, oldStatus, newStatus, currentUser.getFirstName() + " " + currentUser.getLastName());
        return mapToDTO(updatedTask);
    }

    // Duruma göre görevleri filtreleme
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByStatus(Long projectId, Task.TaskStatus status) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Kullanıcının projede üye olup olmadığını kontrol et
        boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, currentUser);
        if (!isMember) {
            throw new RuntimeException("Bu projeye erişim yetkiniz bulunmamaktadır");
        }

        List<Task> tasks = taskRepository.findByProjectAndStatus(project, status);
        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Son teslim tarihine göre görevleri sıralama
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksSortedByDeadline(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        // Kullanıcının projede üye olup olmadığını kontrol et
        boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, currentUser);
        if (!isMember) {
            throw new RuntimeException("Bu projeye erişim yetkiniz bulunmamaktadır");
        }

        List<Task> tasks = taskRepository.findByProjectOrderByDeadlineAsc(project);
        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Görev detayını getirme
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Görev bulunamadı"));

        // Kullanıcının göreve erişim yetkisini kontrol et
        boolean isAssigned = task.getAssignedUsers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
        boolean isProjectAdmin = task.getProject().getCreatedBy().getId().equals(currentUser.getId()) ||
                projectUserRoleRepository.existsByProjectAndUserAndRoleType(
                        task.getProject(), currentUser, ProjectUserRole.ProjectRoleType.PROJECT_ADMIN);
        boolean isProjectMember = projectUserRoleRepository.existsByProjectAndUser(task.getProject(), currentUser);

        if (!isAssigned && !isProjectAdmin && !isProjectMember) {
            throw new RuntimeException("Bu göreve erişim yetkiniz bulunmamaktadır");
        }

        return mapToDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(Long taskId, TaskDTO taskDTO) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Görev bulunamadı"));

        Project project = task.getProject();

        // Yetki kontrolü - sadece görevin atandığı kişi veya proje yöneticisi güncelleyebilir
        boolean isProjectAdmin = project.isProjectAdmin(currentUser);
        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());

        if (!isProjectAdmin && !isCreator) {
            throw new UnauthorizedException("Bu görevi güncelleme yetkiniz bulunmamaktadır");
        }

        // Temel bilgileri güncelle
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDeadline(taskDTO.getDeadline());
        task.setLastModifiedBy(currentUser.getEmail());

        // Atanan kullanıcıları güncelle
        if (taskDTO.getAssignedUserIds() != null) {
            Set<User> assignedUsers = new HashSet<>();
            for (Long userId : taskDTO.getAssignedUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Atanacak kullanıcı bulunamadı: " + userId));

                // Kullanıcının projede üye olup olmadığını kontrol et
                boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, user);
                if (!isMember) {
                    throw new RuntimeException("Kullanıcı " + user.getEmail() + " bu projenin üyesi değil");
                }
                assignedUsers.add(user);
            }
            task.setAssignedUsers(assignedUsers);
        }

        Task updatedTask = taskRepository.save(task);
        activityLogService.logTaskActivity(
                updatedTask,
                "TASK_UPDATED",
                "Görev güncellendi: " + task.getTitle(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        return mapToDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Görev bulunamadı"));

        Project project = task.getProject();
        if (!project.isProjectAdmin(currentUser) &&
                !task.getCreatedBy().equals(currentUser)){
            throw new UnauthorizedException("Bu görevi silme yetkiniz yok");
        }

        taskRepository.delete(task);

        activityLogService.logTaskActivity(task, "TASK_DELETED", "Görev silindi: " + task.getTitle(), currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    // DTO'ya dönüştürme yardımcı metodu
    private TaskDTO mapToDTO(Task task) {
        Set<Long> userIds = new HashSet<>();
        Set<String> userEmails = new HashSet<>();

        if (task.getAssignedUsers() != null) {
            for (User user : task.getAssignedUsers()) {
                userIds.add(user.getId());
                userEmails.add(user.getEmail());
            }
        }

        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .deadline(task.getDeadline())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedUserIds(userIds)
                .assignedUserEmails(userEmails)
                .createdByEmail(task.getCreatedBy().getEmail())
                .build();
    }
}