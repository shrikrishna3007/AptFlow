package com.project.aptflow.service;

import com.project.aptflow.dto.booking.BookingDTO;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    void roomBook(BookingDTO booking);

    List<BookingDTO> getAllBookings();

    BookingDTO updateBooking(Long id, BookingDTO updatedBooking);

    BookingDTO getBookingById(Long id);

    void deleteBooking(Long id);

    void cancelBooking(Long id);

    void setBookingTerm(BookingDTO bookingDTO);

    BookingDTO updateCheckOutDate(Long id, LocalDate checkOut);
}
