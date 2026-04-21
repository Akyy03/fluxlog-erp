package com.techflow.erp.controller;

import com.techflow.erp.constant.TaskStatus;
import com.techflow.erp.dto.TaskDTO;
import com.techflow.erp.entity.Employee;
import com.techflow.erp.entity.Task;
import com.techflow.erp.entity.User;
import com.techflow.erp.repository.EmployeeRepository;
import com.techflow.erp.repository.TaskRepository;
import com.techflow.erp.repository.UserRepository;
import com.techflow.erp.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO dto) {
        return ResponseEntity.ok(taskService.createTask(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle((String) payload.get("title"));
        task.setDescription((String) payload.get("description"));

        if (payload.get("deadline") != null) {
            task.setDeadline(LocalDate.parse((String) payload.get("deadline")));
        }

        // Rezolvare cu USER în loc de EMPLOYEE:
        if (payload.get("assignedToId") != null) {
            Long userId = Long.valueOf(payload.get("assignedToId").toString());

            // Căutăm în UserRepository, nu în EmployeeRepository
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            task.setAssignedTo(user); // Acum tipurile se potrivesc
        }

        Task updatedTask = taskRepository.save(task);

        // Returnăm un DTO sau un simplu mesaj de succes pentru a evita erorile de serializare
        return ResponseEntity.ok().body(Map.of("message", "Task updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-tasks/{userId}") // Temporar punem userId in path
    public ResponseEntity<List<TaskDTO>> getMyTasks(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

}