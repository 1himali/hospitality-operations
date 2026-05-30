package com.hospitality.operations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hospitality.operations.auth.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // ── PAYROLL — Owner only ──
                        .requestMatchers("/api/v1/admin/payroll/**").hasRole("OWNER")

                        // ── USER MANAGEMENT — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/users/**").hasAnyRole("ADMIN", "OWNER")

                        // ── SYSTEM CONFIG — Owner only ──
                        .requestMatchers("/api/v1/admin/config/**").hasRole("OWNER")

                        // ── MOCK MODE — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/mock-mode/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ANALYTICS — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/analytics/**").hasAnyRole("ADMIN", "OWNER")

                        // ── API METRICS (including export) — Admin / Owner only ──
                        .requestMatchers("/api/v1/metrics/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ASSISTANT ADMIN — Admin / Owner only (enable/disable) ──
                        .requestMatchers("/api/v1/assistant/admin/**").hasAnyRole("ADMIN", "OWNER")

                        // ── All other API paths — any authenticated user ──
                        .requestMatchers("/api/v1/**").authenticated()

                        // ── Catch-all ──
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
