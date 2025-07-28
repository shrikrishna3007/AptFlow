package com.project.aptflow.dto.billing;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDTO {
    private String adhaarNumber;
    private String name;
    private String email;
}
