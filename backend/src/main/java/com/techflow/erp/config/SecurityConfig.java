package com.techflow.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var opt = new org.springframework.web.cors.CorsConfiguration();
                    opt.setAllowedOrigins(java.util.List.of("http://localhost:4200")); // Permite Angular
                    opt.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    opt.setAllowedHeaders(java.util.List.of("*"));
                    return opt;
                }))
                .csrf(csrf -> csrf.disable()) // Important pentru API-uri
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Momentan lăsăm totul liber
                );

        return http.build();
    }
}