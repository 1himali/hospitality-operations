package com.hospitality.operations.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create test user if it doesn't exist
        if (!userRepository.existsByUsername("user")) {
            User testUser = User.builder()
                    .username("user")
                    .passwordHash(passwordEncoder.encode("1234"))
                    .role(UserRole.ROLE_USER)
                    .tenantSchema("default")
                    .build();
            userRepository.save(testUser);
            System.out.println("✓ Test user created: username=user, password=1234");
        }
    }
}
