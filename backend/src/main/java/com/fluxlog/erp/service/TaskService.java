package com.fluxlog.erp.service;

import com.fluxlog.erp.constant.TaskStatus;
import com.fluxlog.erp.dto.TaskDTO;
import com.fluxlog.erp.entity.Task;
import com.fluxlog.erp.entity.User;
import com.fluxlog.erp.repository.ProjectRepository;
import com.fluxlog.erp.repository.TaskRepository;
import com.fluxlog.erp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<TaskDTO> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TaskDTO convertToDTO(Task task) {
        String assignedName = "Unassigned";

        User assignee = task.getAssignedTo();
        if (assignee != null) {
            if (assignee.getEmployee() != null) {
                assignedName = assignee.getEmployee().getFirstName() + " " + assignee.getEmployee().getLastName();
            } else {
                assignedName = assignee.getEmail();
            }
        }

        Long pId = null;
        String pName = "Fără Proiect";

        if (task.getProject() != null) {
            pId = task.getProject().getId();
            pName = task.getProject().getName();
        }

        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .assignedToName(assignedName)
                .projectId(pId)
                .projectName(pName)
                .build();
    }

    public TaskDTO updateTaskStatus(Long taskId, String newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.valueOf(newStatus));

        return convertToDTO(taskRepository.save(task));
    }

    @Transactional
    public void updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(newStatus);
        taskRepository.save(task);
    }

    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(TaskStatus.TODO)
                .project(projectRepository.getReferenceById(dto.getProjectId()))
                .assignedTo(dto.getAssignedToId() != null ?
                        userRepository.getReferenceById(dto.getAssignedToId()) : null)
                .build();

        return convertToDTO(taskRepository.save(task));
    }

    public List<TaskDTO> getTasksByUserId(Long userId) {
        return taskRepository.findByAssignedToId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getRecentTasks() {
        return taskRepository.findTop3ByStatusNotOrderByDeadlineAsc(TaskStatus.DONE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTaskStatistics() {
        return Arrays.stream(TaskStatus.values())
                .map(status -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", status.name());
                    map.put("value", taskRepository.countByStatus(status));
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getUpcomingTasksByDepartment(Long deptId) {
        Pageable topFive = PageRequest.of(0, 3);

        return taskRepository.findUpcomingTasksByDepartment(deptId, TaskStatus.DONE, topFive)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}