package com.techflow.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private BigDecimal salary;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String hireDate;
    private String role;
    private String temporaryPassword;
    private boolean isDeleted;
}