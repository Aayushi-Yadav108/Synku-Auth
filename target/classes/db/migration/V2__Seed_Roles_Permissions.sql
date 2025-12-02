-- V2: Seed Default Roles and Permissions

-- Insert default roles
INSERT INTO roles (role_name, role_description) VALUES
('SUPER_ADMIN', 'Super administrator with full system access'),
('CAMPUS_ADMIN', 'Campus administrator'),
('STUDENT', 'Student user'),
('COMPANY_RECRUITER', 'Company recruiter')
ON DUPLICATE KEY UPDATE role_description = VALUES(role_description);

-- Insert default permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('VIEW_DASHBOARD', 'dashboard', 'view', 'View dashboard'),
('MANAGE_CAMPUSES', 'campus', 'manage', 'Full campus management'),
('MANAGE_STUDENTS', 'student', 'manage', 'Full student management'),
('MANAGE_COMPANIES', 'company', 'manage', 'Full company management'),
('VIEW_ANALYTICS', 'analytics', 'view', 'View analytics')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Assign all permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'),
    permission_id
FROM permissions
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

