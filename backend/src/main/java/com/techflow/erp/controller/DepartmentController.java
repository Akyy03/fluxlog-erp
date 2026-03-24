package com.techflow.erp.controller;

import com.techflow.erp.entity.Department;
import com.techflow.erp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})public class DepartmentController {

    private final DepartmentService departmentService;

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(departmentService.saveDepartment(department));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentDetails));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-department")
    public ResponseEntity<Department> getMyDepartment(@RequestHeader("User-Email") String email) {
        return ResponseEntity.ok(departmentService.getDepartmentByManagerEmail(email));
    }

    @PatchMapping("/my-department/description")
    public ResponseEntity<Department> updateDescription(
            @RequestHeader("User-Email") String email,
            @RequestBody Map<String, String> body) { // Folosește Map ca să citești cheia "description"

        String newDescription = body.get("description");
        Department updated = departmentService.updateDepartmentDescription(email, newDescription);
        return ResponseEntity.ok(updated);
    }
}