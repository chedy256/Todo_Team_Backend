package com.chedyProjects.TodoTeam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    // Tasks owned by this user
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Task> ownedTasks;

    // Tasks assigned to this user
    @OneToMany(mappedBy = "assigned", cascade = CascadeType.ALL)
    private Set<Task> assignedTasks;
}

