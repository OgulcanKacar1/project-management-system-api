package com.example.PMS01.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"project", "assignedUser", "createdBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@DynamicUpdate // Sadece değişen alanlar için güncelleme sorgusu oluşturur
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToMany
    @JoinTable(
            name = "task_assigned_users",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedUsers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Transient
    private TaskStatus previousStatus;

    public enum TaskStatus {
        TODO,        // Yapılacak
        IN_PROGRESS, // Devam Ediyor
        COMPLETED    // Tamamlandı
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PostLoad
    protected void onLoad() {
        this.previousStatus = this.status;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Task.java sınıfına ekleyin
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskChatMessage> chatMessages = new HashSet<>();

    public boolean isStatusChanged() {
        return previousStatus != null && !previousStatus.equals(status);
    }

    public String generateStatusChangeLogMessage() {
        if (previousStatus == null) {
            return String.format("Görev durumu '%s' olarak ayarlandı", status);
        }
        return String.format("Görev durumu '%s' -> '%s' olarak değiştirildi", previousStatus, status);
    }
}