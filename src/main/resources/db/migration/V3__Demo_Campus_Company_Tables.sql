-- V3: Demo Tables (Campus & Company) - For Reference Only
-- NOTE: In production, these should be in separate microservices
-- This is just for demo/testing purposes in Auth Service

-- Campus profiles table (DEMO - should be in Campus Service)
CREATE TABLE IF NOT EXISTS campus_profiles (
    campus_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    campus_name VARCHAR(255) NOT NULL,
    campus_logo_url VARCHAR(500),
    campus_rank INT,
    admin_name VARCHAR(255) NOT NULL,
    admin_email VARCHAR(255) NOT NULL,
    admin_phone VARCHAR(20) NOT NULL,
    admin_department VARCHAR(100),
    admin_designation VARCHAR(100),
    website_url VARCHAR(500),
    about_campus TEXT,
    campus_address VARCHAR(500) NOT NULL,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB COMMENT='DEMO: Campus profiles - Move to Campus Service';

-- Company profiles table (DEMO - should be in Company Service)
CREATE TABLE IF NOT EXISTS company_profiles (
    company_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    company_logo_url VARCHAR(500),
    admin_name VARCHAR(255) NOT NULL,
    admin_designation VARCHAR(100) NOT NULL,
    admin_email VARCHAR(255) NOT NULL,
    admin_phone VARCHAR(20) NOT NULL,
    website_url VARCHAR(500),
    other_website_url VARCHAR(500),
    register_number VARCHAR(100),
    about_company TEXT,
    company_address VARCHAR(500) NOT NULL,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB COMMENT='DEMO: Company profiles - Move to Company Service';

-- Company key people table (DEMO - should be in Company Service)
CREATE TABLE IF NOT EXISTS company_key_people (
    key_person_id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    photo_url VARCHAR(500),
    designation VARCHAR(100) NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company_profiles(company_id) ON DELETE CASCADE,
    INDEX idx_company_id (company_id)
) ENGINE=InnoDB COMMENT='DEMO: Company key people - Move to Company Service';

