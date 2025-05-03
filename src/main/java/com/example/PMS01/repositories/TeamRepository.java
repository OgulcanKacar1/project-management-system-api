package com.example.PMS01.repositories;

import com.example.PMS01.entities.Team;
import com.example.PMS01.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByLeader(User leader);
    List<Team> findByMembersContaining(User member);
}
