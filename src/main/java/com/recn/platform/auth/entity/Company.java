package com.recn.platform.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @UuidGenerator
    @Column(name = "company_id", length = 36, nullable = false, updatable = false)
    private String companyId;

    @Column(name = "user_id", length = 36, nullable = false, unique = true)
    private String userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_logo_url", length = 500)
    private String companyLogoUrl;

    @Column(name = "admin_name", nullable = false)
    private String adminName;

    @Column(name = "admin_designation", nullable = false, length = 100)
    private String adminDesignation;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "admin_phone", nullable = false)
    private String adminPhone;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "other_website_url", length = 500)
    private String otherWebsiteUrl;

    @Column(name = "register_number", length = 100)
    private String registerNumber;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompanyKeyPerson> keyPeople = new ArrayList<>();

    @Column(name = "about_company", columnDefinition = "TEXT")
    private String aboutCompany;

    @Column(name = "company_address", length = 500, nullable = false)
    private String companyAddress;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus = "PENDING";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to add key person
    public void addKeyPerson(CompanyKeyPerson keyPerson) {
        keyPeople.add(keyPerson);
        keyPerson.setCompany(this);
    }
}

