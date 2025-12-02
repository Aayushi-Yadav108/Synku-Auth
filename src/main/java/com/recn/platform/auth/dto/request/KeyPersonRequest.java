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
public class KeyPersonRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String photoUrl;

    @NotBlank(message = "Designation is required")
    private String designation;
}

