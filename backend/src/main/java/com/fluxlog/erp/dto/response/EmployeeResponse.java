package com.fluxlog.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Integer remainingLeaveDays;

    @JsonProperty("isDeleted")
    private boolean isDeleted;
}