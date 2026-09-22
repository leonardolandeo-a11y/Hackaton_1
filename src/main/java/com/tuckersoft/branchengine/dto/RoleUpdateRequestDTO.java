package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleUpdateRequestDTO {

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "ROLE_USER|ROLE_ADMIN",
            message = "Role must be ROLE_USER or ROLE_ADMIN"
    )
    private String role;
}
