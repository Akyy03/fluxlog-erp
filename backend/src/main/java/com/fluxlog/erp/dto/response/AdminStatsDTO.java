package com.fluxlog.erp.dto.response;

public record AdminStatsDTO(
        long totalEmployees,
        long activeProjects,
        long pendingLeaves,
        long urgentTasks
) { }