package com.project.aptflow.dto.auth;

import com.project.aptflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDTO {
    private String adhaarNumber;
    private String name;
    private String address;
    private String gender;
    private String mobileNumber;
    private String email;
    private String password;
    private Role role;
}
