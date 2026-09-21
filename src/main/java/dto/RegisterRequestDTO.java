package dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {

    @Email
    @NotBlank
    private String email;

    @Size(min = 6)
    private String password;
}