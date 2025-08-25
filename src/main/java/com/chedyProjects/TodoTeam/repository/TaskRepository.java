package com.chedyProjects.TodoTeam.repository;

import com.chedyProjects.TodoTeam.entity.Task;
import com.chedyProjects.TodoTeam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.assigned IS NULL OR t.owner = :user OR t.assigned = :user")
    List<Task> findAccessibleTasks(@Param("user") User user);

    @Query("SELECT t FROM Task t WHERE t.id = :id AND (t.assigned IS NULL OR t.owner = :user OR t.assigned = :user)")
    Optional<Task> findAccessibleTaskById(@Param("id") Long id, @Param("user") User user);
}

