package com.recn.platform.auth.controller;

import com.recn.platform.auth.dto.request.CompanyRegistrationRequest;
import com.recn.platform.auth.dto.response.ApiResponse;
import com.recn.platform.auth.dto.response.CompanyRegistrationResponse;
import com.recn.platform.auth.security.UserPrincipal;
import com.recn.platform.auth.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Company Registration Controller
 * 
 * Handles company profile registration after user selects "Register as Company"
 */
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyRegistrationController {

    private final CompanyService companyService;

    /**
     * Company Profile Registration
     * Called after user registers and selects userType=COMPANY
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('COMPANY_RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyRegistrationResponse>> registerCompany(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CompanyRegistrationRequest request
    ) {
        log.info("Company registration request from user: {}", userPrincipal.getUserId());

        // Validate user type
        if (!"COMPANY".equals(userPrincipal.getUserType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only company users can register company profiles", 403));
        }

        // Register company
        CompanyRegistrationResponse response = companyService.registerCompany(
                userPrincipal.getUserId(), 
                request
        );

        log.info("Company registered successfully with ID: {}", response.getCompanyId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company registered successfully. Pending admin approval.", response));
    }

    /**
     * Get Company Profile
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyRegistrationResponse>> getCompany(
            @PathVariable String companyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Get company profile request for ID: {}", companyId);

        CompanyRegistrationResponse response = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(ApiResponse.success("Company profile retrieved", response));
    }

    /**
     * Update Company Profile
     */
    @PutMapping("/{companyId}")
    @PreAuthorize("hasRole('COMPANY_RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyRegistrationResponse>> updateCompany(
            @PathVariable String companyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CompanyRegistrationRequest request
    ) {
        log.info("Update company profile request for ID: {}", companyId);

        CompanyRegistrationResponse response = companyService.updateCompany(
                companyId, 
                userPrincipal.getUserId(), 
                request
        );
        
        return ResponseEntity.ok(ApiResponse.success("Company profile updated", response));
    }
}

