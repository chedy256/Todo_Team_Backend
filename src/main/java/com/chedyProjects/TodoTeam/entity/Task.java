package com.chedyProjects.TodoTeam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority;

    @Builder.Default
    @Column(name = "is_completed")
    private boolean isCompleted = false;

    @Column(name = "due_date")
    private Long dueDate; // Unix timestamp

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_id")
    private User assigned;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdate = Instant.now();
    }

    public enum Priority {
        LOW, NORMAL, HIGH
    }
}
