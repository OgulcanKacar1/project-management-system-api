package com.example.PMS01.repositories;

import com.example.PMS01.entities.Team;
import com.example.PMS01.entities.TeamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamTypeRepository extends JpaRepository<TeamType, Long> {
    Optional<TeamType> findByName(String name);
    boolean existsByName(String name);
}
