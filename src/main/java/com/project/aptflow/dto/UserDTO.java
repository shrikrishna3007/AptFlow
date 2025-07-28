package com.project.aptflow.dto;

import com.project.aptflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/*
This DTO used only for methods like read, update and delete operations in UserController.
That is the reason password not included here.
 */

@Setter
@Getter
@AllArgsConstructor
public class UserDTO {
    private String adhaarNumber;
    private String name;
    private String address;
    private String gender;
    private String mobileNumber;
    private String email;
    private Role role;
}
