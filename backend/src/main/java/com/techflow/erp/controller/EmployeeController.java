package com.techflow.erp.controller;

import com.techflow.erp.dto.response.EmployeeResponse;
import com.techflow.erp.entity.Employee;
import com.techflow.erp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authorities: " + auth.getAuthorities());

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        if (isAdmin) {
            System.out.println("Sunt ADMIN, cer toată lista.");
            return ResponseEntity.ok(employeeService.getAllEmployeesIncludeDeleted());
        }
        return ResponseEntity.ok(employeeService.getAllActiveEmployees());
    }

    @GetMapping("/me/{email}")
    public ResponseEntity<Employee> getMyProfile(@PathVariable String email) {
        return ResponseEntity.ok(employeeService.findByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> addEmployee(@RequestBody Employee employee) {
        return new ResponseEntity<>(employeeService.addEmployee(employee), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Employee> restoreEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.restoreEmployee(id));
    }

    @PutMapping("/me/update/{email}")
    public ResponseEntity<Employee> updateMyProfile(@PathVariable String email, @RequestBody Employee updatedData) {
        return ResponseEntity.ok(employeeService.updateOwnProfile(email, updatedData));
    }
}