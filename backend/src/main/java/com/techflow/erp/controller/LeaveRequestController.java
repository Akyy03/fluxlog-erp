package com.techflow.erp.controller;

import com.techflow.erp.constant.LeaveStatus;
import com.techflow.erp.entity.LeaveRequest;
import com.techflow.erp.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // Folosește userId pentru creare
    @PostMapping("/employee/{userId}")
    public ResponseEntity<LeaveRequest> createRequest(
            @PathVariable Long userId,
            @RequestBody LeaveRequest request) {
        return ResponseEntity.ok(leaveRequestService.createRequest(userId, request));
    }

    // Folosește userId pentru istoric
    @GetMapping("/employee/{userId}")
    public ResponseEntity<List<LeaveRequest>> getMyRequests(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveRequestService.getRequestsByUserId(userId));
    }

    @GetMapping("/department/{deptId}/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingForDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(leaveRequestService.getPendingRequestsForDepartment(deptId));
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<LeaveRequest> processRequest(
            @PathVariable Long requestId,
            @RequestParam LeaveStatus status,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(leaveRequestService.approveOrRejectRequest(requestId, status, comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        leaveRequestService.deleteRequest(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<LeaveRequest>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(leaveRequestService.getDepartmentRequests(deptId));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllRequests());
    }

    @GetMapping("/employee/{userId}/balance")
    public ResponseEntity<Integer> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveRequestService.getRemainingDays(userId));
    }
}