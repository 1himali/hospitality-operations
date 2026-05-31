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
        // Create user if it doesn't exist
        if (!userRepository.existsByUsername("user")) {
            User testUser = User.builder()
                    .username("user")
                    .passwordHash(passwordEncoder.encode("1234"))
                    .role(UserRole.ROLE_USER)
                    .tenantSchema("default")
                    .build();
            userRepository.save(testUser);
            System.out.println("✓ User created: username=user, password=1234");
        }

        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("abcd"))
                    .role(UserRole.ROLE_ADMIN)
                    .tenantSchema("default")
                    .build();
            userRepository.save(adminUser);
            System.out.println("✓ Admin created: username=admin, password=abcd");
        }

        // Create owner user if it doesn't exist
        if (!userRepository.existsByUsername("owner")) {
            User ownerUser = User.builder()
                    .username("owner")
                    .passwordHash(passwordEncoder.encode("4321"))
                    .role(UserRole.ROLE_OWNER)
                    .tenantSchema("default")
                    .build();
            userRepository.save(ownerUser);
            System.out.println("✓ Owner created: username=owner, password=4321");
        }
    }
}
