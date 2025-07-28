package com.project.aptflow.dto.booking;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserBookingUpdateDTO {
    private LocalDate checkOut;
}
