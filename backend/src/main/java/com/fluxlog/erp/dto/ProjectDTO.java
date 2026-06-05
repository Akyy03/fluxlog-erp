package com.fluxlog.erp.dto;

import com.fluxlog.erp.constant.ProjectStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private ProjectStatus status;
    private int progress;
    private Set<Long> departmentIds;
    private String createdByUserName;
}