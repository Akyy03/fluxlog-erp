package com.fluxlog.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private boolean needsPasswordChange;
    private Long userId;
    private Long departmentId;
}