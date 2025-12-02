package com.recn.platform.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRegistrationRequest {

    // Company Basic Info
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    private String companyLogoUrl;  // Upload photo

    // Admin Details
    @NotBlank(message = "Admin name is required")
    @Size(max = 255, message = "Admin name must not exceed 255 characters")
    private String adminName;

    @NotBlank(message = "Admin designation is required")
    @Size(max = 100, message = "Admin designation must not exceed 100 characters")
    private String adminDesignation;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    private String adminEmail;

    @NotBlank(message = "Admin phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Admin phone number must be valid")
    private String adminPhone;

    // Company URLs
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+.*$", 
             message = "Company website URL must be valid")
    private String websiteUrl;

    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+.*$", 
             message = "Other website URL must be valid")
    private String otherWebsiteUrl;

    // Register Number (like company registration number, CIN, etc.)
    @Size(max = 100, message = "Register number must not exceed 100 characters")
    private String registerNumber;

    // Key People (dynamic list)
    @Valid
    private List<KeyPersonRequest> keyPeople;

    // Company Description
    @Size(max = 2000, message = "About company must not exceed 2000 characters")
    private String aboutCompany;

    @NotBlank(message = "Company address is required")
    @Size(max = 500, message = "Company address must not exceed 500 characters")
    private String companyAddress;
}

