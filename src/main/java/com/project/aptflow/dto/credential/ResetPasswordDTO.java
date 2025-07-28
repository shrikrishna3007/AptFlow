package com.project.aptflow.dto.credential;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordDTO {

    private String token;
    private String newPassword;

}
