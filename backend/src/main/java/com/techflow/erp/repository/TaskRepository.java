package com.techflow.erp.repository;

import com.techflow.erp.constant.TaskStatus;
import com.techflow.erp.entity.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Toate task-urile dintr-un proiect
    List<Task> findByProjectId(Long projectId);

    // Toate task-urile asignate unui om (pentru "My Tasks")
    List<Task> findByAssignedToId(Long userId);

    // Numarul de task-uri dintr-un proiect cu un anumit status (ne ajuta la progres)
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    // Numarul total de task-uri dintr-un proiect
    long countByProjectId(Long projectId);

    long countByStatusNotAndDeadlineBefore(TaskStatus status, LocalDate deadline);

    List<Task> findTop3ByStatusNotOrderByDeadlineAsc(TaskStatus status);

    long countByStatus(TaskStatus status);

    @Query("SELECT t.status, COUNT(t) " +
            "FROM Task t " +
            "JOIN t.assignedTo u " +
            "JOIN u.employee e " +
            "WHERE e.department.id = :deptId " +
            "GROUP BY t.status")
    List<Object[]> countTaskStatsByTeam(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(t) FROM Task t JOIN t.project p JOIN p.departments d " +
            "WHERE d.id = :deptId " +
            "AND t.status <> :statusDone " +
            "AND t.deadline <= :cutoffDate")
    long countUrgentTasksByDepartmentId(@Param("deptId") Long deptId,
                                        @Param("statusDone") TaskStatus statusDone,
                                        @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT t FROM Task t JOIN t.project p JOIN p.departments d " +
            "WHERE d.id = :deptId AND t.status <> :status " +
            "ORDER BY t.deadline ASC")
    List<Task> findUpcomingTasksByDepartment(@Param("deptId") Long deptId,
                                             @Param("status") TaskStatus status,
                                             Pageable pageable);

    List<Task> findByAssignedTo_Employee_Department_Id(Long deptId);
}