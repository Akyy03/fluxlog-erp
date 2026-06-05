package com.fluxlog.erp.dto;

import com.fluxlog.erp.constant.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private String assignedToName;
    private LocalDate deadline;
    private Long projectId;
    private String projectName;
    private Long assignedToId;
}