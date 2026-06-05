package com.fluxlog.erp.controller;

import com.fluxlog.erp.dto.TaskDTO;
import com.fluxlog.erp.dto.response.AdminStatsDTO;
import com.fluxlog.erp.service.StatsService;
import com.fluxlog.erp.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StatsService statsService;
    private final TaskService taskService;

    @GetMapping("/admin-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsDTO getAdminStats() {
        return statsService.getAdminDashboardStats();
    }

    @GetMapping("/recent-tasks")
    public ResponseEntity<List<TaskDTO>> getRecentTasks() {
        return ResponseEntity.ok(taskService.getRecentTasks());
    }

    @GetMapping("/task-stats")
    public ResponseEntity<List<Map<String, Object>>> getTaskStats() {
        return ResponseEntity.ok(taskService.getTaskStatistics());
    }

    @GetMapping("/manager/{deptId}/tasks")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getManagerTaskStats(@PathVariable Long deptId) {
        return ResponseEntity.ok(statsService.getTaskStats(deptId));
    }

    @GetMapping("/manager/{deptId}/projects")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getManagerProjectStats(@PathVariable Long deptId) {
        return ResponseEntity.ok(statsService.getProjectStats(deptId));
    }

    @GetMapping("/manager/{deptId}/stats")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getManagerStats(@PathVariable Long deptId) {
        return ResponseEntity.ok(statsService.getManagerStats(deptId));
    }

    @GetMapping("/manager/{deptId}/upcoming-tasks")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<List<TaskDTO>> getUpcomingTasks(@PathVariable Long deptId) {
        return ResponseEntity.ok(taskService.getUpcomingTasksByDepartment(deptId));
    }
}