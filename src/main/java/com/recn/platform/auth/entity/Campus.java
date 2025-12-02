package com.recn.platform.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "campus_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campus {

    @Id
    @UuidGenerator
    @Column(name = "campus_id", length = 36, nullable = false, updatable = false)
    private String campusId;

    @Column(name = "user_id", length = 36, nullable = false, unique = true)
    private String userId;

    @Column(name = "campus_name", nullable = false)
    private String campusName;

    @Column(name = "campus_logo_url", length = 500)
    private String campusLogoUrl;

    @Column(name = "campus_rank")
    private Integer campusRank;

    @Column(name = "admin_name", nullable = false)
    private String adminName;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "admin_phone", nullable = false)
    private String adminPhone;

    @Column(name = "admin_department", length = 100)
    private String adminDepartment;

    @Column(name = "admin_designation", length = 100)
    private String adminDesignation;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "about_campus", columnDefinition = "TEXT")
    private String aboutCampus;

    @Column(name = "campus_address", length = 500, nullable = false)
    private String campusAddress;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus = "PENDING";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

