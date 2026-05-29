-- Insert test user with username "user" and password "1234"
-- BCrypt hash for "1234" with cost 10
INSERT INTO users (username, password_hash, role, tenant_schema, created_at)
VALUES ('user', '$2a$10$H2XvjQV2mWBQc7pKJL6aJe8xvO5cZwMGqOjF1u9pY7gS4c9kZ8Jme', 'ROLE_USER', 'default', NOW())
ON CONFLICT (username) DO NOTHING;
