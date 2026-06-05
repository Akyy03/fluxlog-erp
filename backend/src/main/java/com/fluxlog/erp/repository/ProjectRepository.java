package com.fluxlog.erp.repository;

import com.fluxlog.erp.constant.ProjectStatus;
import com.fluxlog.erp.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Gasim proiectele create de un anumit utilizator (Manager/Admin)
    List<Project> findByCreatedById(Long userId);

    List<Project> findAllByDepartmentsId(Long departmentId);

    long countByStatus(ProjectStatus status);

    @Query("SELECT p.status, COUNT(p) FROM Project p JOIN p.departments d WHERE d.id = :deptId GROUP BY p.status")
    List<Object[]> countProjectStatsByDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(p) FROM Project p JOIN p.departments d WHERE d.id = :deptId AND p.status = com.fluxlog.erp.constant.ProjectStatus.ACTIVE")
    long countActiveProjectsByDepartmentId(@Param("deptId") Long deptId);
}