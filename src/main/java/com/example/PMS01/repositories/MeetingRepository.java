package com.example.PMS01.repositories;

import com.example.PMS01.entities.Meeting;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByProjectId(Long projectId);
    List<Meeting> findByParticipantsContaining(User user);
    List<Meeting> findByParticipantsContainingAndStartTimeAfter(User user, LocalDateTime dateTime);

}
