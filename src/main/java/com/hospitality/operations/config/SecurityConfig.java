package com.hospitality.operations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
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

                        // ── PAYROLL — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/payroll/**").hasAnyRole("ADMIN", "OWNER")

                        // ── USER MANAGEMENT — Admin only ──
                        .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")

                        // ── SYSTEM CONFIG — Owner only ──
                        .requestMatchers("/api/v1/admin/config/**").hasRole("OWNER")

                        // ── MOCK MODE — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/mock-mode/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ANALYTICS — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/analytics/**").hasAnyRole("ADMIN", "OWNER")

                        // ── API METRICS (including export) — Admin / Owner only ──
                        .requestMatchers("/api/v1/metrics/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ACTIVITY — Admin / Owner only ──
                        .requestMatchers("/api/v1/admin/activity/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ASSISTANT ADMIN — Admin / Owner only (enable/disable) ──
                        .requestMatchers("/api/v1/assistant/admin/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ACTION ITEMS — Admin / Owner / Manager only ──
                        .requestMatchers("/api/v1/action-items/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ── INVENTORY — Admin / Owner / Manager only (CRUD access) ──
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ── BILL/INVOICE WRITE — Admin / Owner only (editing prices, invoices) ──
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bills/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/bills/**").hasAnyRole("ADMIN", "OWNER")

                        // ── BILL LIST & EXPORT — Admin / Owner / Manager only (User cannot view invoice history) ──
                        .requestMatchers(HttpMethod.GET, "/api/v1/bills").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/bills/export/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ── MENU WRITE — Admin / Owner only (price editing) ──
                        .requestMatchers(HttpMethod.POST, "/api/v1/menu/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/menu/**").hasAnyRole("ADMIN", "OWNER")

                        // ── ROOMS WRITE — Admin / Owner only (rate editing) ──
                        .requestMatchers(HttpMethod.POST, "/api/v1/rooms/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/rooms/**").hasAnyRole("ADMIN", "OWNER")

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
