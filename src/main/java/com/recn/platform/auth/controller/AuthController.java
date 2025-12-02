package com.recn.platform.auth.controller;

import com.recn.platform.auth.dto.request.LoginRequest;
import com.recn.platform.auth.dto.request.ProfileCompletionRequest;
import com.recn.platform.auth.dto.request.RefreshTokenRequest;
import com.recn.platform.auth.dto.request.RegisterRequest;
import com.recn.platform.auth.dto.request.SelectUserTypeRequest;
import com.recn.platform.auth.dto.response.ApiResponse;
import com.recn.platform.auth.dto.response.AuthResponse;
import com.recn.platform.auth.security.UserPrincipal;
import com.recn.platform.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Logout request received for user: {}", userPrincipal.getUserId());
        authService.logout(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Get current user request for: {}", userPrincipal.getUsername());
        
        var userData = new Object() {
            public final String userId = userPrincipal.getUserId();
            public final String email = userPrincipal.getUsername();
            public final String userType = userPrincipal.getUserType();
            public final var roles = userPrincipal.getRoleNames();
            public final var permissions = userPrincipal.getPermissionNames();
        };
        
        return ResponseEntity.ok(ApiResponse.success(userData));
    }

    @PostMapping("/profile/complete")
    public ResponseEntity<ApiResponse<Void>> markProfileCompleted(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ProfileCompletionRequest request
    ) {
        log.info("Mark profile completed request for user: {}", userPrincipal.getUserId());
        authService.markProfileCompleted(userPrincipal.getUserId(), request.getProfileServiceId());
        return ResponseEntity.ok(ApiResponse.success("Profile marked as completed", null));
    }

    @PostMapping("/select-user-type")
    public ResponseEntity<ApiResponse<Void>> selectUserType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SelectUserTypeRequest request
    ) {
        log.info("Select user type request for user: {} -> {}", userPrincipal.getUserId(), request.getUserType());
        authService.updateUserType(userPrincipal.getUserId(), request.getUserType());
        return ResponseEntity.ok(ApiResponse.success("User type updated successfully", null));
    }
}

