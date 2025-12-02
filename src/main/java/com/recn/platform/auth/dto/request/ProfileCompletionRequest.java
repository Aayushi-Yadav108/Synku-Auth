package com.recn.platform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCompletionRequest {

    @NotBlank(message = "Profile service ID is required")
    private String profileServiceId;  // ID from the respective service (campus_id, student_id, etc.)
}

