package com.fluxlog.erp.controller;

import com.fluxlog.erp.config.JwtService;
import com.fluxlog.erp.dto.request.LoginRequest;
import com.fluxlog.erp.dto.response.AuthResponse;
import com.fluxlog.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

                        Long deptId = (user.getEmployee() != null && user.getEmployee().getDepartment() != null)
                                ? user.getEmployee().getDepartment().getId()
                                : null;

                        return ResponseEntity.ok(new AuthResponse(
                                token,
                                user.getEmail(),
                                user.getRole().name(),
                                user.isNeedsPasswordChange(),
                                user.getId(),
                                deptId
                        ));
                    }
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found"));
    }
}
