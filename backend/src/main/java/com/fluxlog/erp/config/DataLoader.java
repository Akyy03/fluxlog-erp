package com.fluxlog.erp.config;

import com.fluxlog.erp.constant.Role;
import com.fluxlog.erp.entity.User;
import com.fluxlog.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {

            User admin = new User();
            admin.setEmail("admin@fluxlog.ro");
            admin.setPassword(passwordEncoder.encode("admin123")); // Parola criptată
            admin.setRole(Role.ADMIN); // Setează rolul de ADMIN

            userRepository.save(admin);

            System.out.println("Sistem FluxLog Initializat cu succes!");
            System.out.println("Admin: admin@fluxlog.ro / admin123");
        }
    }
}
