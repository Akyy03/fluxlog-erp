package com.fluxlog.erp.repository;

import com.fluxlog.erp.constant.LeaveStatus;
import com.fluxlog.erp.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :deptId AND lr.status = 'PENDING'")
    List<LeaveRequest> findAllPendingByDepartment(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l WHERE l.employee.id = :empId " +
            "AND l.status != 'REJECTED' " +
            "AND (l.startDate <= :endDate AND l.endDate >= :startDate)")
    boolean existsByEmployeeIdAndStatusNot(Long empId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l WHERE l.employee.id = :employeeId " +
            "AND l.status != 'REJECTED' " +
            "AND ((:start BETWEEN l.startDate AND l.endDate) OR (:end BETWEEN l.startDate AND l.endDate))")
    boolean existsOverlappingRequest(@Param("employeeId") Long employeeId,
                                     @Param("start") LocalDate start,
                                     @Param("end") LocalDate end);

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployee_Department_Id(Long deptId);

    List<LeaveRequest> findByEmployee_Department_IdAndStatus(Long deptId, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.user.id = :userId ORDER BY lr.startDate DESC")
    List<LeaveRequest> findAllByUserId(@Param("userId") Long userId);

    long countByStatus(LeaveStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.department.id = :deptId AND lr.status = 'PENDING'")
    long countPendingByDepartmentId(@Param("deptId") Long deptId);
}