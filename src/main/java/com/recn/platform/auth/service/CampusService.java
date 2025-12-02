package com.recn.platform.auth.service;

import com.recn.platform.auth.dto.request.CampusRegistrationRequest;
import com.recn.platform.auth.dto.response.CampusRegistrationResponse;
import com.recn.platform.auth.entity.Campus;
import com.recn.platform.auth.exception.ResourceAlreadyExistsException;
import com.recn.platform.auth.exception.ResourceNotFoundException;
import com.recn.platform.auth.repository.CampusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampusService {

    private final CampusRepository campusRepository;

    @Transactional
    public CampusRegistrationResponse registerCampus(String userId, CampusRegistrationRequest request) {
        log.info("Registering campus for user: {}", userId);

        // Check if campus already exists for this user
        if (campusRepository.existsByUserId(userId)) {
            throw new ResourceAlreadyExistsException("Campus profile already exists for this user");
        }

        // Create campus entity
        Campus campus = Campus.builder()
                .userId(userId)
                .campusName(request.getCampusName())
                .campusLogoUrl(request.getCampusLogoUrl())
                .campusRank(request.getCampusRank())
                .adminName(request.getAdminName())
                .adminEmail(request.getAdminEmail())
                .adminPhone(request.getAdminPhone())
                .adminDepartment(request.getAdminDepartment())
                .adminDesignation(request.getAdminDesignation())
                .websiteUrl(request.getWebsiteUrl())
                .aboutCampus(request.getAboutCampus())
                .campusAddress(request.getCampusAddress())
                .approvalStatus("PENDING")
                .build();

        campus = campusRepository.save(campus);
        log.info("Campus registered successfully with ID: {}", campus.getCampusId());

        return mapToResponse(campus);
    }

    @Transactional(readOnly = true)
    public CampusRegistrationResponse getCampusByUserId(String userId) {
        Campus campus = campusRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found for user: " + userId));
        return mapToResponse(campus);
    }

    @Transactional(readOnly = true)
    public CampusRegistrationResponse getCampusById(String campusId) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found with ID: " + campusId));
        return mapToResponse(campus);
    }

    @Transactional
    public CampusRegistrationResponse updateCampus(String campusId, String userId, CampusRegistrationRequest request) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found with ID: " + campusId));

        // Verify ownership
        if (!campus.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Campus not found or access denied");
        }

        // Update fields
        campus.setCampusName(request.getCampusName());
        campus.setCampusLogoUrl(request.getCampusLogoUrl());
        campus.setCampusRank(request.getCampusRank());
        campus.setAdminName(request.getAdminName());
        campus.setAdminEmail(request.getAdminEmail());
        campus.setAdminPhone(request.getAdminPhone());
        campus.setAdminDepartment(request.getAdminDepartment());
        campus.setAdminDesignation(request.getAdminDesignation());
        campus.setWebsiteUrl(request.getWebsiteUrl());
        campus.setAboutCampus(request.getAboutCampus());
        campus.setCampusAddress(request.getCampusAddress());

        campus = campusRepository.save(campus);
        log.info("Campus updated successfully: {}", campusId);

        return mapToResponse(campus);
    }

    private CampusRegistrationResponse mapToResponse(Campus campus) {
        return CampusRegistrationResponse.builder()
                .campusId(campus.getCampusId())
                .userId(campus.getUserId())
                .campusName(campus.getCampusName())
                .campusLogoUrl(campus.getCampusLogoUrl())
                .campusRank(campus.getCampusRank())
                .adminName(campus.getAdminName())
                .adminEmail(campus.getAdminEmail())
                .adminPhone(campus.getAdminPhone())
                .websiteUrl(campus.getWebsiteUrl())
                .approvalStatus(campus.getApprovalStatus())
                .createdAt(campus.getCreatedAt())
                .build();
    }
}

