package com.recn.platform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusRegistrationResponse {

    private String campusId;
    private String userId;
    private String campusName;
    private String campusLogoUrl;
    private Integer campusRank;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String websiteUrl;
    private String approvalStatus;  // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
}

