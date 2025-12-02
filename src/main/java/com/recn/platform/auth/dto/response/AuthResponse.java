package com.recn.platform.auth.dto.response;

import com.recn.platform.auth.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String userId;
    private String email;
    private UserType userType;
    private Set<String> roles;
    private Set<String> permissions;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
    private String tokenType = "Bearer";
    
    // New fields for multi-service flow
    private Boolean profileCompleted;
    private String profileServiceId;
    private String redirectTo;  // Which service to redirect to
}

