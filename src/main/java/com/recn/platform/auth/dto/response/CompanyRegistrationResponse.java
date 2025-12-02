package com.recn.platform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRegistrationResponse {

    private String companyId;
    private String userId;
    private String companyName;
    private String companyLogoUrl;
    private String adminName;
    private String adminDesignation;
    private String adminEmail;
    private String adminPhone;
    private String websiteUrl;
    private String otherWebsiteUrl;
    private String registerNumber;
    private List<KeyPersonResponse> keyPeople;
    private String approvalStatus;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyPersonResponse {
        private String name;
        private String photoUrl;
        private String designation;
    }
}

