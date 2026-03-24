package com.techflow.erp.repository;

import com.techflow.erp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.manager.email = :email")
    Optional<Department> findByManagerEmail(@Param("email") String email);}