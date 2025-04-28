package com.example.PMS01.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="project_team")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    @Column(nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ProjectTeamMember> uyeler = new ArrayList<>();

    public void addUye(ProjectTeamMember uye) {
        uyeler.add(uye);
        uye.setTeam(this);
    }
}
