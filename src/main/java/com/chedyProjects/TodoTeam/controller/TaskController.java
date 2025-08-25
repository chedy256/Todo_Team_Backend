package com.chedyProjects.TodoTeam.controller;

import com.chedyProjects.TodoTeam.dto.TaskDto;
import com.chedyProjects.TodoTeam.entity.User;
import com.chedyProjects.TodoTeam.service.TaskService;
import com.chedyProjects.TodoTeam.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;

    @GetMapping
    public List<TaskDto> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getById(getUserId(userDetails));
        return taskService.getAccessibleTasks(user);
    }

    @GetMapping("/{id}")
    public TaskDto getTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getById(getUserId(userDetails));
        return taskService.getAccessibleTaskById(id, user);
    }

    @PostMapping
    public Map<String, Object> createTask(@Valid @RequestBody CreateTaskRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getById(getUserId(userDetails));
        TaskDto dto = taskService.createTask(req.getTitle(), req.getDescription(), req.getPriority(), req.getDueDate(), req.getAssigneeId(), user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("taskId", dto.getId());
        return resp;
    }

    @PutMapping("/{id}")
    public Map<String, String> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getById(getUserId(userDetails));
        taskService.updateTask(id, req.getDescription(), req.getPriority(), req.getDueDate(), req.getAssigneeId(), user);
        return Map.of("status", "success");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getById(getUserId(userDetails));
        taskService.deleteTask(id, user);
        return Map.of("status", "success");
    }

    private Long getUserId(UserDetails userDetails) {
        // Fetch User entity by email directly
        User user = userService.getByEmail(userDetails.getUsername());
        if (user == null) throw new RuntimeException("User not found");
        return user.getId();
    }

    @Data
    public static class CreateTaskRequest {
        @NotBlank
        private String title;
        private String description;
        @NotBlank
        private String priority;
        @NotNull
        private Long dueDate;
        private Long assigneeId;
    }

    @Data
    public static class UpdateTaskRequest {
        private String description;
        private String priority;
        private Long dueDate;
        private Long assigneeId;
    }
}
