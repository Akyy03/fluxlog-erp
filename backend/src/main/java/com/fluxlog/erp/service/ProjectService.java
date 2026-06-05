package com.fluxlog.erp.service;

import com.fluxlog.erp.constant.ProjectStatus;
import com.fluxlog.erp.constant.Role;
import com.fluxlog.erp.constant.TaskStatus;
import com.fluxlog.erp.dto.ProjectDTO;
import com.fluxlog.erp.entity.Department;
import com.fluxlog.erp.entity.Project;
import com.fluxlog.erp.entity.User;
import com.fluxlog.erp.repository.DepartmentRepository;
import com.fluxlog.erp.repository.ProjectRepository;
import com.fluxlog.erp.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final DepartmentRepository departmentRepository;

    public List<ProjectDTO> getProjectsForCurrentUser(User user) {
        if (user == null) return Collections.emptyList();

        List<Project> projects;

        if (user.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else if (user.getRole() == Role.MANAGER && user.getEmployee() != null && user.getEmployee().getDepartment() != null) {
            Long deptId = user.getEmployee().getDepartment().getId();
            projects = projectRepository.findAllByDepartmentsId(deptId);
        } else {
            projects = Collections.emptyList();
        }

        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        return convertToDTO(project);
    }

    private ProjectDTO convertToDTO(Project project) {
        long totalTasks = taskRepository.countByProjectId(project.getId());
        long doneTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        int progressPercent = (totalTasks > 0) ? (int) ((doneTasks * 100) / totalTasks) : 0;

        String fullName = "System Admin";
        User creator = project.getCreatedBy();
        if (creator != null) {
            if (creator.getEmployee() != null) {
                fullName = creator.getEmployee().getFirstName() + " " + creator.getEmployee().getLastName();
            } else {
                fullName = creator.getEmail();
            }
        }

        Set<Long> deptIds = (project.getDepartments() != null)
                ? project.getDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toSet())
                : Collections.emptySet();

        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .status(project.getStatus())
                .progress(progressPercent)
                .departmentIds(deptIds)
                .createdByUserName(fullName)
                .build();
    }

    public ProjectDTO saveProject(Project project, User currentUser) {
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.ACTIVE);
        }
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    public List<ProjectDTO> getAllProjectsForDebug() {
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignDepartmentToProject(Long projectId, Long deptId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        project.getDepartments().add(dept);
        projectRepository.save(project);
    }

    @Transactional
    public void removeDepartmentFromProject(Long projectId, Long deptId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.getDepartments().removeIf(d -> d.getId().equals(deptId));
        projectRepository.save(project);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        project.setStatus(dto.getStatus());

        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }

    public long getActiveProjectsCount(Long deptId) {
        return projectRepository.countActiveProjectsByDepartmentId(deptId);
    }
}