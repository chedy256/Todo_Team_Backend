package com.chedyProjects.TodoTeam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private boolean isCompleted;
    private Long dueDate;
    private Long ownerId;
    private Long assignedId;
    private Long lastUpdate;
}

