package com.recn.platform.auth.controller;

import com.recn.platform.auth.dto.request.CampusRegistrationRequest;
import com.recn.platform.auth.dto.response.ApiResponse;
import com.recn.platform.auth.dto.response.CampusRegistrationResponse;
import com.recn.platform.auth.security.UserPrincipal;
import com.recn.platform.auth.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Campus Registration Controller
 * 
 * NOTE: This is a DEMO/EXAMPLE controller in Auth Service.
 * In production, this should be in Campus Service (Port 8082).
 * 
 * This shows the pattern for Phase 2 registration.
 */
@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
@Slf4j
public class CampusRegistrationController {

    private final CampusService campusService;

    /**
     * Campus Profile Registration
     * Called after user registers via /auth/register with userType=CAMPUS
     * 
     * Flow:
     * 1. User registers in Auth Service → Gets JWT
     * 2. Frontend redirects to campus profile form
     * 3. User fills this form → Calls this endpoint
     * 4. This endpoint creates campus profile
     * 5. Returns campus_id
     * 6. Frontend calls /auth/profile/complete with campus_id
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('CAMPUS_ADMIN')")
    public ResponseEntity<ApiResponse<CampusRegistrationResponse>> registerCampus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CampusRegistrationRequest request
    ) {
        log.info("Campus registration request from user: {}", userPrincipal.getUserId());

        // Validate user type
        if (!"CAMPUS".equals(userPrincipal.getUserType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only campus users can register campus profiles", 403));
        }

        // Register campus
        CampusRegistrationResponse response = campusService.registerCampus(
                userPrincipal.getUserId(), 
                request
        );

        log.info("Campus registered successfully with ID: {}", response.getCampusId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campus registered successfully. Pending admin approval.", response));
    }

    /**
     * Get Campus Profile
     */
    @GetMapping("/{campusId}")
    public ResponseEntity<ApiResponse<CampusRegistrationResponse>> getCampus(
            @PathVariable String campusId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Get campus profile request for ID: {}", campusId);

        CampusRegistrationResponse response = campusService.getCampusById(campusId);
        return ResponseEntity.ok(ApiResponse.success("Campus profile retrieved", response));
    }

    /**
     * Update Campus Profile
     */
    @PutMapping("/{campusId}")
    @PreAuthorize("hasRole('CAMPUS_ADMIN')")
    public ResponseEntity<ApiResponse<CampusRegistrationResponse>> updateCampus(
            @PathVariable String campusId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CampusRegistrationRequest request
    ) {
        log.info("Update campus profile request for ID: {}", campusId);

        CampusRegistrationResponse response = campusService.updateCampus(
                campusId, 
                userPrincipal.getUserId(), 
                request
        );
        
        return ResponseEntity.ok(ApiResponse.success("Campus profile updated", response));
    }
}

