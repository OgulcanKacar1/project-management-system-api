package com.example.PMS01.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "team_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "teamType")
    @Builder.Default
    private Set<Team> teams = new HashSet<>();
}