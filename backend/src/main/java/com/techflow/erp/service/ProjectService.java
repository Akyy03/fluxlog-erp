package com.techflow.erp.service;

import com.techflow.erp.constant.ProjectStatus;
import com.techflow.erp.constant.Role;
import com.techflow.erp.constant.TaskStatus;
import com.techflow.erp.dto.ProjectDTO;
import com.techflow.erp.entity.Department;
import com.techflow.erp.entity.Project;
import com.techflow.erp.entity.User;
import com.techflow.erp.repository.DepartmentRepository;
import com.techflow.erp.repository.ProjectRepository;
import com.techflow.erp.repository.TaskRepository;
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
            // Un angajat simplu sau un user fără departament nu vede proiecte globale momentan
            projects = Collections.emptyList();
        }

        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        // Folosim metoda convertToDTO pe care am pus-o la punct împreună
        return convertToDTO(project);
    }

    private ProjectDTO convertToDTO(Project project) {
        // 1. Calcul Progres
        long totalTasks = taskRepository.countByProjectId(project.getId());
        long doneTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        int progressPercent = (totalTasks > 0) ? (int) ((doneTasks * 100) / totalTasks) : 0;

        // 2. Extragere Nume Creator
        String fullName = "System Admin";
        User creator = project.getCreatedBy();
        if (creator != null) {
            if (creator.getEmployee() != null) {
                fullName = creator.getEmployee().getFirstName() + " " + creator.getEmployee().getLastName();
            } else {
                fullName = creator.getEmail();
            }
        }

        // 3. Extragere ID-uri Departamente (cu protecție la null)
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
        // Setăm creatorul automat la salvare
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    // Metodă temporară ca să deblocăm Frontend-ul
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
        projectRepository.save(project); // Aici se scrie în tabela project_departments
    }

    @Transactional
    public void removeDepartmentFromProject(Long projectId, Long deptId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Scoatem departamentul din setul proiectului
        project.getDepartments().removeIf(d -> d.getId().equals(deptId));
        projectRepository.save(project);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Actualizăm doar câmpurile de bază
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