package com.recn.platform.auth.service;

import com.recn.platform.auth.dto.request.CompanyRegistrationRequest;
import com.recn.platform.auth.dto.request.KeyPersonRequest;
import com.recn.platform.auth.dto.response.CompanyRegistrationResponse;
import com.recn.platform.auth.entity.Company;
import com.recn.platform.auth.entity.CompanyKeyPerson;
import com.recn.platform.auth.exception.ResourceAlreadyExistsException;
import com.recn.platform.auth.exception.ResourceNotFoundException;
import com.recn.platform.auth.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyRegistrationResponse registerCompany(String userId, CompanyRegistrationRequest request) {
        log.info("Registering company for user: {}", userId);

        // Check if company already exists for this user
        if (companyRepository.existsByUserId(userId)) {
            throw new ResourceAlreadyExistsException("Company profile already exists for this user");
        }

        // Create company entity
        Company company = Company.builder()
                .userId(userId)
                .companyName(request.getCompanyName())
                .companyLogoUrl(request.getCompanyLogoUrl())
                .adminName(request.getAdminName())
                .adminDesignation(request.getAdminDesignation())
                .adminEmail(request.getAdminEmail())
                .adminPhone(request.getAdminPhone())
                .websiteUrl(request.getWebsiteUrl())
                .otherWebsiteUrl(request.getOtherWebsiteUrl())
                .registerNumber(request.getRegisterNumber())
                .aboutCompany(request.getAboutCompany())
                .companyAddress(request.getCompanyAddress())
                .approvalStatus("PENDING")
                .build();

        // Add key people if provided
        if (request.getKeyPeople() != null && !request.getKeyPeople().isEmpty()) {
            for (KeyPersonRequest keyPersonReq : request.getKeyPeople()) {
                CompanyKeyPerson keyPerson = CompanyKeyPerson.builder()
                        .name(keyPersonReq.getName())
                        .photoUrl(keyPersonReq.getPhotoUrl())
                        .designation(keyPersonReq.getDesignation())
                        .build();
                company.addKeyPerson(keyPerson);
            }
        }

        company = companyRepository.save(company);
        log.info("Company registered successfully with ID: {}", company.getCompanyId());

        return mapToResponse(company);
    }

    @Transactional(readOnly = true)
    public CompanyRegistrationResponse getCompanyByUserId(String userId) {
        Company company = companyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for user: " + userId));
        return mapToResponse(company);
    }

    @Transactional(readOnly = true)
    public CompanyRegistrationResponse getCompanyById(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
        return mapToResponse(company);
    }

    @Transactional
    public CompanyRegistrationResponse updateCompany(String companyId, String userId, CompanyRegistrationRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // Verify ownership
        if (!company.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Company not found or access denied");
        }

        // Update fields
        company.setCompanyName(request.getCompanyName());
        company.setCompanyLogoUrl(request.getCompanyLogoUrl());
        company.setAdminName(request.getAdminName());
        company.setAdminDesignation(request.getAdminDesignation());
        company.setAdminEmail(request.getAdminEmail());
        company.setAdminPhone(request.getAdminPhone());
        company.setWebsiteUrl(request.getWebsiteUrl());
        company.setOtherWebsiteUrl(request.getOtherWebsiteUrl());
        company.setRegisterNumber(request.getRegisterNumber());
        company.setAboutCompany(request.getAboutCompany());
        company.setCompanyAddress(request.getCompanyAddress());

        // Update key people (remove old and add new)
        company.getKeyPeople().clear();
        if (request.getKeyPeople() != null && !request.getKeyPeople().isEmpty()) {
            for (KeyPersonRequest keyPersonReq : request.getKeyPeople()) {
                CompanyKeyPerson keyPerson = CompanyKeyPerson.builder()
                        .name(keyPersonReq.getName())
                        .photoUrl(keyPersonReq.getPhotoUrl())
                        .designation(keyPersonReq.getDesignation())
                        .build();
                company.addKeyPerson(keyPerson);
            }
        }

        company = companyRepository.save(company);
        log.info("Company updated successfully: {}", companyId);

        return mapToResponse(company);
    }

    private CompanyRegistrationResponse mapToResponse(Company company) {
        List<CompanyRegistrationResponse.KeyPersonResponse> keyPeopleResponse = company.getKeyPeople().stream()
                .map(kp -> CompanyRegistrationResponse.KeyPersonResponse.builder()
                        .name(kp.getName())
                        .photoUrl(kp.getPhotoUrl())
                        .designation(kp.getDesignation())
                        .build())
                .collect(Collectors.toList());

        return CompanyRegistrationResponse.builder()
                .companyId(company.getCompanyId())
                .userId(company.getUserId())
                .companyName(company.getCompanyName())
                .companyLogoUrl(company.getCompanyLogoUrl())
                .adminName(company.getAdminName())
                .adminDesignation(company.getAdminDesignation())
                .adminEmail(company.getAdminEmail())
                .adminPhone(company.getAdminPhone())
                .websiteUrl(company.getWebsiteUrl())
                .otherWebsiteUrl(company.getOtherWebsiteUrl())
                .registerNumber(company.getRegisterNumber())
                .keyPeople(keyPeopleResponse)
                .approvalStatus(company.getApprovalStatus())
                .createdAt(company.getCreatedAt())
                .build();
    }
}

