package com.techflow.erp.service;

import com.techflow.erp.entity.Department;
import com.techflow.erp.entity.Employee;
import com.techflow.erp.repository.DepartmentRepository;
import com.techflow.erp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }

    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department details) {
        Department dept = getDepartmentById(id);
        dept.setName(details.getName());
        dept.setDescription(details.getDescription());
        dept.setManager(details.getManager());
        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        Department dept = getDepartmentById(id);
        departmentRepository.delete(dept);
    }

    // DepartmentService.java
    public Department getDepartmentByManagerEmail(String email) {
        // 1. Găsim angajatul după email
        // Presupunem că ai metoda findByEmail în EmployeeRepository
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Angajat negăsit: " + email));

        // 2. Returnăm departamentul lui
        if (emp.getDepartment() == null) {
            throw new RuntimeException("Acest manager nu este alocat niciunui departament!");
        }

        return emp.getDepartment();
    }

    public Department updateDepartmentDescription(String email, String description) {
        String cleanEmail = email.trim(); // Just in case
        System.out.println("Căutăm departament pentru managerul: [" + cleanEmail + "]");

        Department dept = departmentRepository.findByManagerEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Department not found for this manager: " + cleanEmail));

        dept.setDescription(description);
        return departmentRepository.save(dept);
    }
}