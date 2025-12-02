package com.recn.platform.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusRegistrationRequest {

    // Campus Basic Info
    @NotBlank(message = "Campus name is required")
    @Size(max = 255, message = "Campus name must not exceed 255 characters")
    private String campusName;

    private String campusLogoUrl;  // Uploaded photo URL (handle file upload separately)

    private Integer campusRank;

    // Admin Details
    @NotBlank(message = "Admin name is required")
    @Size(max = 255, message = "Admin name must not exceed 255 characters")
    private String adminName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    private String adminEmail;

    @NotBlank(message = "Admin phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Admin phone number must be valid")
    private String adminPhone;

    @Size(max = 100, message = "Admin department must not exceed 100 characters")
    private String adminDepartment;

    @Size(max = 100, message = "Admin designation must not exceed 100 characters")
    private String adminDesignation;

    // Campus Info
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+.*$", 
             message = "Campus website URL must be valid")
    private String websiteUrl;

    @Size(max = 2000, message = "About campus must not exceed 2000 characters")
    private String aboutCampus;

    @NotBlank(message = "Campus address is required")
    @Size(max = 500, message = "Campus address must not exceed 500 characters")
    private String campusAddress;

    // Optional: You might want to add these
    private String city;
    private String state;
    private String pincode;
}

