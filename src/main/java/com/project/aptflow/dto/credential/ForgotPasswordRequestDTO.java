package com.project.aptflow.dto.credential;

import jakarta.validation.constraints.Email;

public class ForgotPasswordRequestDTO {
    @Email
    private String email;

    public @Email String getEmail() {
        return email;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }
}
