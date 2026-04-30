package com.techflow.erp.controller;

import com.techflow.erp.dto.ProjectDTO;
import com.techflow.erp.entity.Project;
import com.techflow.erp.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getProjects() {
        System.out.println(">>> Request primit pe /api/projects - Bypass Security Mode");
        // Chemăm direct metoda care dă TOT, fără să mai întrebăm cine e userul
        return ResponseEntity.ok(projectService.getAllProjectsForDebug());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        // Chemăm service-ul să ne dea proiectul convertit deja în DTO
        ProjectDTO projectDTO = projectService.getProjectById(id);
        return ResponseEntity.ok(projectDTO);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody Project project) {
        // Punem un user dummy sau null momentan pentru salvare
        return ResponseEntity.ok(projectService.saveProject(project, null));
    }

    @PostMapping("/{projectId}/departments/{deptId}")
    public ResponseEntity<Void> assignDepartment(@PathVariable Long projectId, @PathVariable Long deptId) {
        projectService.assignDepartmentToProject(projectId, deptId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/departments/{deptId}")
    public ResponseEntity<Void> removeDepartment(@PathVariable Long projectId, @PathVariable Long deptId) {
        projectService.removeDepartmentFromProject(projectId, deptId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/active/{deptId}")
    public ResponseEntity<Long> getActiveProjectsCount(@PathVariable Long deptId) {
        return ResponseEntity.ok(projectService.getActiveProjectsCount(deptId));
    }
}