package com.techflow.erp.repository;

import com.techflow.erp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.user")
    List<Employee> findAllWithUser();

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") Long userId);

    Optional<Employee> findByUserEmail(String email);

    Optional<Employee> findByEmail(String email);
}