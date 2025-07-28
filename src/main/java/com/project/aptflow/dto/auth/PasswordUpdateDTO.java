package com.project.aptflow.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordUpdateDTO {
    private String email;
    private String oldPassword;
    private String newPassword;

}
