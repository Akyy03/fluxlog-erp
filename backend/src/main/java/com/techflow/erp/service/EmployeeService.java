package com.techflow.erp.service;

import com.techflow.erp.constant.Role;
import com.techflow.erp.dto.response.EmployeeResponse;
import com.techflow.erp.entity.Employee;
import com.techflow.erp.entity.User;
import com.techflow.erp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String ALLOWED_DOMAIN = "@techflow.com";

    public List<EmployeeResponse> getAllActiveEmployees() {
        return getAllEmployeesIncludeDeleted(); // Forțează arhiva peste tot pentru test
    }

    public List<EmployeeResponse> getAllEmployeesIncludeDeleted() {
        // Folosim Query-ul NATIV care ignoră orice setare de @Where a Hibernate-ului
        List<Employee> employees = employeeRepository.findAllNative();
        System.out.println("DEBUG SQL NATIV: Am găsit " + employees.size() + " rânduri.");
        return employees.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
        employee.setDeleted(true);
        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
        }
        employeeRepository.save(employee);
    }

    @Transactional
    public Employee restoreEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setDeleted(false);
        if (employee.getUser() != null) {
            employee.getUser().setActive(true);
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public EmployeeResponse addEmployee(Employee employee) {
        validateEmailDomain(employee.getEmail());
        if (employee.getUser() == null) {
            employee.setUser(createAutomaticUser(employee.getEmail()));
        }
        employee.setRole(employee.getUser().getRole().name());
        calculateLeaveDays(employee);
        return mapToResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, Employee empDetails) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (empDetails.getEmail() != null) {
            validateEmailDomain(empDetails.getEmail());
            if (employee.getUser() != null) employee.getUser().setEmail(empDetails.getEmail());
        }

        if (empDetails.getRole() != null && employee.getUser() != null) {
            Role newRole = Role.valueOf(empDetails.getRole());
            employee.getUser().setRole(newRole);
            employee.setRole(newRole.name());
        }

        employee.setFirstName(empDetails.getFirstName());
        employee.setLastName(empDetails.getLastName());
        employee.setPosition(empDetails.getPosition());
        employee.setSalary(empDetails.getSalary());
        employee.setPhone(empDetails.getPhone());
        if (empDetails.getHireDate() != null) employee.setHireDate(empDetails.getHireDate());
        if (empDetails.getDepartment() != null) employee.setDepartment(empDetails.getDepartment());

        calculateLeaveDays(employee);
        return mapToResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    // --- Metode pentru Profilul Meu ---
    public Employee findByEmail(String email) {
        return employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Profilul nu a fost găsit pentru email: " + email));
    }

    @Transactional
    public Employee updateOwnProfile(String email, Employee updatedData) {
        Employee existingEmployee = findByEmail(email);
        User currentUser = existingEmployee.getUser();

        if (updatedData.getPhone() != null) {
            existingEmployee.setPhone(updatedData.getPhone());
        }

        if (updatedData.getUser() != null) {
            User userUpdate = updatedData.getUser();
            if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty()) {
                currentUser.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
                currentUser.setTempPasswordPlain(null);
            }
        }
        return employeeRepository.save(existingEmployee);
    }

    // --- Helpers ---
    private User createAutomaticUser(String email) {
        User user = new User();
        user.setEmail(email);
        String tempPass = UUID.randomUUID().toString().substring(0, 10);
        user.setPassword(passwordEncoder.encode(tempPass));
        user.setTempPasswordPlain(tempPass);
        user.setRole(Role.EMPLOYEE);
        user.setActive(true);
        user.setNeedsPasswordChange(true);
        return user;
    }

    private EmployeeResponse mapToResponse(Employee emp) {
        // 1. Folosim ID-ul de Employee, nu de User!
        Long employeeId = emp.getId();

        // 2. Protecție la null pentru User
        String email = (emp.getUser() != null) ? emp.getUser().getEmail() : emp.getEmail();
        String currentRole = (emp.getUser() != null) ? emp.getUser().getRole().name() : "EMPLOYEE";
        String tempPass = (emp.getUser() != null) ? emp.getUser().getTempPasswordPlain() : null;

        // 3. PROTECȚIE CRITICĂ la Department (Aici crapă arhiva de obicei)
        Long deptId = (emp.getDepartment() != null) ? emp.getDepartment().getId() : null;
        String deptName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "Fără Departament";

        return new EmployeeResponse(
                employeeId,
                emp.getFirstName(),
                emp.getLastName(),
                email,
                emp.getPosition(),
                emp.getSalary(),
                emp.getPhone(),
                deptId,
                deptName,
                emp.getHireDate() != null ? emp.getHireDate().toString() : null,
                currentRole,
                tempPass,
                emp.isDeleted() // Asigură-te că getter-ul returnează ce trebuie
        );
    }

    private void validateEmailDomain(String email) {
        if (email == null || !email.toLowerCase().endsWith(ALLOWED_DOMAIN)) {
            throw new IllegalArgumentException("Email must belong to " + ALLOWED_DOMAIN);
        }
    }

    private void calculateLeaveDays(Employee employee) {
        if ("ADMIN".equalsIgnoreCase(employee.getRole())) {
            employee.setTotalLeaveDays(0);
            employee.setRemainingLeaveDays(0);
            return;
        }
        int baseDays = "MANAGER".equalsIgnoreCase(employee.getRole()) ? 23 : 21;
        int seniority = (employee.getHireDate() != null) ?
                Period.between(employee.getHireDate(), LocalDate.now()).getYears() : 0;
        int total = Math.min(baseDays + seniority, 28);
        employee.setTotalLeaveDays(total);
        if (employee.getRemainingLeaveDays() == null) employee.setRemainingLeaveDays(total);
    }
}