package com.hospitality.operations.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.auth.User;
import com.hospitality.operations.auth.UserRepository;
import com.hospitality.operations.auth.UserRole;
import com.hospitality.operations.domain.activity.AuditHelper;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final AuditHelper auditHelper;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_OWNER) {
            auditHelper.record("DELETE", "USER", id, "Blocked deletion of " + user.getRole() + ": " + user.getUsername());
            throw new IllegalArgumentException("Cannot delete " + user.getRole() + " account: " + user.getUsername());
        }

        userRepository.delete(user);
        auditHelper.record("DELETE", "USER", id, "Deleted user: " + user.getUsername());
        return ResponseEntity.ok(Map.of("message", "User deleted: " + user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String roleStr = body.get("role");

        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password are required");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        UserRole role = UserRole.ROLE_USER;
        if (roleStr != null) {
            try {
                role = UserRole.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + roleStr);
            }
        }

        org.springframework.security.crypto.password.PasswordEncoder encoder =
                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class
                        .cast(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());

        User user = User.builder()
                .username(username)
                .passwordHash(encoder.encode(password))
                .role(role)
                .tenantSchema(body.getOrDefault("tenantSchema", "default"))
                .build();

        User created = userRepository.save(user);
        auditHelper.record("CREATE", "USER", created.getId(), "Created user: " + created.getUsername() + " with role " + created.getRole());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }

        org.springframework.security.crypto.password.PasswordEncoder encoder =
                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class
                        .cast(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());

        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);
        auditHelper.record("UPDATE", "USER", id, "Password reset for user: " + user.getUsername());

        return ResponseEntity.ok(Map.of("message", "Password reset successful for user: " + user.getUsername()));
    }
}
