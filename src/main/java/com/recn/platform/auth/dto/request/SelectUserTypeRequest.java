package com.recn.platform.auth.dto.request;

import com.recn.platform.auth.enums.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectUserTypeRequest {

    @NotNull(message = "User type is required")
    private UserType userType;
}

