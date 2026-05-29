package com.hospitality.operations.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hospitality.operations.auth.dto.AuthResponseDto;
import com.hospitality.operations.auth.dto.LoginRequestDto;
import com.hospitality.operations.auth.dto.RegisterRequestDto;
import com.hospitality.operations.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(
                user.getUsername(), user.getRole().name(), user.getTenantSchema());

        return AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .tenantSchema(user.getTenantSchema())
                .build();
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_USER)
                .tenantSchema(request.getTenantSchema() != null ? request.getTenantSchema() : "default")
                .build();

        User saved = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                saved.getUsername(), saved.getRole().name(), saved.getTenantSchema());

        return AuthResponseDto.builder()
                .token(token)
                .username(saved.getUsername())
                .role(saved.getRole().name())
                .tenantSchema(saved.getTenantSchema())
                .build();
    }
}
