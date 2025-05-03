package com.example.PMS01.repositories;

import com.example.PMS01.entities.Comment;
import com.example.PMS01.entities.Task;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);
    List<Comment> findByUser(User user);
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);
}
