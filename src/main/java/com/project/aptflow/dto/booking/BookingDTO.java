package com.project.aptflow.dto.booking;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/*
Used only when create and get all method was called.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long id;
    private UserDTO userDTO;
    private RoomDTO roomDTO;
    private BillDTO billDTO;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BookingStatus state;
    private boolean cancelled;
}
