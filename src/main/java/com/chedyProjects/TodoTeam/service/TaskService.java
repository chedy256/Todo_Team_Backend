package com.chedyProjects.TodoTeam.service;

import com.chedyProjects.TodoTeam.dto.TaskDto;
import com.chedyProjects.TodoTeam.entity.Task;
import com.chedyProjects.TodoTeam.entity.User;
import com.chedyProjects.TodoTeam.repository.TaskRepository;
import com.chedyProjects.TodoTeam.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskDto> getAccessibleTasks(User user) {
        System.out.println("Getting accessible tasks for user: " + user.getId() + " (" + user.getEmail() + ")");
        List<Task> tasks = taskRepository.findAccessibleTasks(user.getId());
        System.out.println("Found " + tasks.size() + " accessible tasks");
        for (Task task : tasks) {
            System.out.println("  Task ID: " + task.getId() + 
                             ", Title: " + task.getTitle() + 
                             ", Owner: " + (task.getOwner() != null ? task.getOwner().getId() : "null") + 
                             ", Assigned: " + (task.getAssigned() != null ? task.getAssigned().getId() : "null"));
        }
        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TaskDto getAccessibleTaskById(Long id, User user) {
        Task task = taskRepository.findAccessibleTaskById(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
        return toDto(task);
    }

    @Transactional
    public TaskDto createTask(String title, String description, String priority, Long dueDate, Long assigneeId, User owner) {
        Task.Priority prio;
        try {
            prio = Task.Priority.valueOf(priority);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority");
        }
        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        }
        Task task = Task.builder()
                .title(title)
                .description(description)
                .priority(prio)
                .isCompleted(false)
                .dueDate(dueDate)
                .owner(owner)
                .assigned(assignee)
                .build();
        taskRepository.save(task);
        return toDto(task);
    }

    @Transactional
    public TaskDto updateTask(Long id, String description, String priority, Long dueDate, Long assigneeId, Boolean completed, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        
        // Check if this is a self-assignment to an unassigned task (green flag)
        boolean isSelfAssignmentToUnassignedTask = task.getAssigned() == null && 
                                                   assigneeId != null && 
                                                   assigneeId.equals(user.getId()) &&
                                                   description == null && 
                                                   priority == null && 
                                                   dueDate == null &&
                                                   completed == null;
        
        // For most operations, user must be owner OR it's a valid self-assignment
        boolean isOwner = task.getOwner().getId().equals(user.getId());
        boolean isAssignee = task.getAssigned() != null && task.getAssigned().getId().equals(user.getId());
        
        // For non-completion updates, allow if user is owner OR if it's a valid self-assignment
        if (!isOwner && !isSelfAssignmentToUnassignedTask) {
            // If only trying to update completion status, allow if user is assignee or owner
            if (completed != null && description == null && priority == null && dueDate == null && assigneeId == null) {
                if (!isAssignee && !isOwner) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or assignee can update completion status");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can update task details");
            }
        }
        
        if (description != null) task.setDescription(description);
        if (priority != null) {
            try {
                task.setPriority(Task.Priority.valueOf(priority));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority");
            }
        }
        if (dueDate != null) task.setDueDate(dueDate);
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
            task.setAssigned(assignee);
        }
        if (completed != null) {
            task.setCompleted(completed);
        }
        taskRepository.save(task);
        return toDto(task);
    }

    @Transactional
    public void deleteTask(Long id, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!task.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can delete");
        }
        taskRepository.delete(task);
    }

    private TaskDto toDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority().name(),
                task.isCompleted(),
                task.getDueDate(),
                task.getOwner() != null ? task.getOwner().getId() : null,
                task.getAssigned() != null ? task.getAssigned().getId() : null,
                task.getLastUpdate() != null ? task.getLastUpdate().toEpochMilli() : null
        );
    }
}

