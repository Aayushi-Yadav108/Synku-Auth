package com.recn.platform.auth.service;

import com.recn.platform.auth.dto.request.LoginRequest;
import com.recn.platform.auth.dto.request.RefreshTokenRequest;
import com.recn.platform.auth.dto.request.RegisterRequest;
import com.recn.platform.auth.dto.response.AuthResponse;
import com.recn.platform.auth.entity.RefreshToken;
import com.recn.platform.auth.entity.Role;
import com.recn.platform.auth.entity.User;
import com.recn.platform.auth.enums.UserType;
import com.recn.platform.auth.exception.AccountLockedException;
import com.recn.platform.auth.exception.InvalidTokenException;
import com.recn.platform.auth.exception.ResourceAlreadyExistsException;
import com.recn.platform.auth.exception.ResourceNotFoundException;
import com.recn.platform.auth.repository.RefreshTokenRepository;
import com.recn.platform.auth.repository.RoleRepository;
import com.recn.platform.auth.repository.UserRepository;
import com.recn.platform.auth.security.JwtUtil;
import com.recn.platform.auth.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-duration-minutes}")
    private int lockDurationMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // User type will be set later in "Register As" page if not provided
        UserType userType = request.getUserType();
        if (userType == null) {
            userType = UserType.STUDENT; // Temporary default, will be updated
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .userType(userType)
                .isVerified(false)
                .isActive(true)
                .profileCompleted(false)
                .failedLoginAttempts(0)
                .build();

        // Assign default role based on user type
        Role defaultRole = getDefaultRoleForUserType(userType);
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getUserId());

        // Generate tokens
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtUtil.generateAccessToken(userPrincipal, user.getUserId(), user.getUserType().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Save refresh token
        saveRefreshToken(user, refreshToken, null);

        return buildAuthResponse(user, accessToken, refreshToken, userPrincipal);
    }

    @Transactional
    public void updateUserType(String userId, UserType userType) {
        log.info("Updating user type for user: {} to {}", userId, userType);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setUserType(userType);
        
        // Update role based on new user type
        Role newRole = getDefaultRoleForUserType(userType);
        Set<Role> roles = new HashSet<>();
        roles.add(newRole);
        user.setRoles(roles);
        
        userRepository.save(user);
        log.info("User type updated successfully");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmailWithRolesAndPermissions(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Account locked for user: {}", request.getEmail());
            throw new AccountLockedException(
                    "Account is locked until: " + user.getAccountLockedUntil() + 
                    ". Please try again later."
            );
        }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedAttempts();
            }
            
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(userPrincipal, user.getUserId(), user.getUserType().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // Save refresh token with request details
            saveRefreshToken(user, refreshToken, httpRequest);

            log.info("User logged in successfully: {}", request.getEmail());
            return buildAuthResponse(user, accessToken, refreshToken, userPrincipal);

        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        String refreshTokenValue = request.getRefreshToken();

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        // Check if token is valid
        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is revoked or expired");
        }

        // Get user
        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = new UserPrincipal(user);

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                userPrincipal, 
                user.getUserId(), 
                user.getUserType().name()
        );

        // Optionally generate new refresh token (rotation)
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        // Revoke old refresh token
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        // Save new refresh token
        saveRefreshToken(user, newRefreshToken, null);

        log.info("Token refreshed successfully for user: {}", user.getEmail());
        return buildAuthResponse(user, newAccessToken, newRefreshToken, userPrincipal);
    }

    @Transactional
    public void logout(String userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Transactional
    public void markProfileCompleted(String userId, String profileServiceId) {
        log.info("Marking profile as completed for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setProfileCompleted(true);
        user.setProfileServiceId(profileServiceId);
        userRepository.save(user);
        
        log.info("Profile marked as completed for user: {}", userId);
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();
        
        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.lockAccount(lockDurationMinutes);
            log.warn("Account locked for user: {} after {} failed attempts", 
                    user.getEmail(), maxFailedAttempts);
        }
        
        userRepository.save(user);
    }

    private void saveRefreshToken(User user, String token, HttpServletRequest request) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000))
                .isRevoked(false)
                .build();

        if (request != null) {
            refreshToken.setIpAddress(getClientIp(request));
            refreshToken.setUserAgent(request.getHeader("User-Agent"));
        }

        refreshTokenRepository.save(refreshToken);
    }

    private Role getDefaultRoleForUserType(UserType userType) {
        String roleName = switch (userType) {
            case CAMPUS -> "CAMPUS_ADMIN";
            case STUDENT -> "STUDENT";
            case COMPANY -> "COMPANY_RECRUITER";
            case ADMIN -> "SUPER_ADMIN";
        };

        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + roleName));
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken, UserPrincipal userPrincipal) {
        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userType(user.getUserType())
                .roles(userPrincipal.getRoleNames())
                .permissions(userPrincipal.getPermissionNames())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiration())
                .tokenType("Bearer")
                .profileCompleted(user.getProfileCompleted())
                .profileServiceId(user.getProfileServiceId())
                .redirectTo(getRedirectService(user.getUserType()))
                .build();
    }

    private String getRedirectService(UserType userType) {
        return switch (userType) {
            case CAMPUS -> "campus-service";
            case STUDENT -> "student-service";
            case COMPANY -> "company-service";
            case ADMIN -> "admin-service";
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

