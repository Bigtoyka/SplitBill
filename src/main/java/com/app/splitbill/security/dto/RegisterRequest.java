package com.app.splitbill.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String password;
}
