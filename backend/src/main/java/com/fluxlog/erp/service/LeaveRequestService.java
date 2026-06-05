package com.fluxlog.erp.service;

import com.fluxlog.erp.constant.LeaveStatus;
import com.fluxlog.erp.entity.Employee;
import com.fluxlog.erp.entity.LeaveRequest;
import com.fluxlog.erp.repository.EmployeeRepository;
import com.fluxlog.erp.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveCalculationService leaveCalculationService;

    @Transactional
    public LeaveRequest createRequest(Long userId, LeaveRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Acest cont nu are un profil de angajat asociat."));

        int workDays = leaveCalculationService.calculateWorkDays(request.getStartDate(), request.getEndDate());

        if (workDays == 0) {
            throw new RuntimeException("Perioada selectată conține doar zile libere.");
        }

        if (employee.getRemainingLeaveDays() < workDays) {
            throw new RuntimeException("Zile insuficiente! Ai: " + employee.getRemainingLeaveDays());
        }

        request.setEmployee(employee);
        request.setWorkDays(workDays);
        request.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(request);
    }

    public List<LeaveRequest> getRequestsByUserId(Long userId) {
        return leaveRequestRepository.findAllByUserId(userId);
    }

    public List<LeaveRequest> getPendingRequestsForDepartment(Long deptId) {
        return leaveRequestRepository.findAllPendingByDepartment(deptId);
    }

    @Transactional
    public LeaveRequest approveOrRejectRequest(Long requestId, LeaveStatus status, String managerComment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Cererea nu a fost găsită."));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Doar cererile în așteptare pot fi procesate.");
        }

        request.setStatus(status);
        request.setManagerComment(managerComment);

        if (status == LeaveStatus.APPROVED) {
            Employee employee = request.getEmployee();
            int remaining = employee.getRemainingLeaveDays() - request.getWorkDays();

            if (remaining < 0) {
                throw new RuntimeException("Eroare: Zile insuficiente!");
            }

            employee.setRemainingLeaveDays(remaining);
            employeeRepository.save(employee);
        }

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public void deleteRequest(Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Cannot delete a processed request.");
        }

        leaveRequestRepository.deleteById(id);
    }

    public List<LeaveRequest> getAllRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getDepartmentRequests(Long deptId) {
        return leaveRequestRepository.findByEmployee_Department_Id(deptId);
    }

    public Integer getRemainingDays(Long userId) {
        return employeeRepository.findByUserId(userId)
                .map(Employee::getRemainingLeaveDays)
                .orElse(0);
    }
}