package com.project.aptflow.controller;

import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.booking.UserBookingUpdateDTO;
import com.project.aptflow.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/book")
    public ResponseEntity<MessageResponseDTO> roomBook(@RequestBody BookingDTO booking){
        bookingService.roomBook(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDTO("Room is booked successfully",HttpStatus.CREATED));
    }

    //method to get all bookings using dto
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<List<BookingDTO>> getAllBookings(){
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.getAllBookings());
    }

    /**
     * Method to update the booking using dto class object
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<BookingDTO>> updateBooking(@PathVariable Long id, @RequestBody BookingDTO updatedBooking) {
        BookingDTO updateBooking= bookingService.updateBooking(id, updatedBooking);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Data updated successfully...", HttpStatus.OK,updateBooking));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/users/{id}")
    public ResponseEntity<ResponseDTO<BookingDTO>> updateCheckOutDate(@PathVariable Long id, @RequestBody UserBookingUpdateDTO userBookingUpdateDTO){
        BookingDTO updatedBooking = bookingService.updateCheckOutDate(id,userBookingUpdateDTO.getCheckOut());
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Data updated successfully...", HttpStatus.OK,updatedBooking));
    }

    //method to cancel booking by id.
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<MessageResponseDTO> cancelBooking(@PathVariable Long id){
        bookingService.cancelBooking(id);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("Booking canceled successfully...",HttpStatus.OK));
    }

    //method to get booking by id.
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id){
        BookingDTO bookingDTO = bookingService.getBookingById(id);
        return ResponseEntity.status(HttpStatus.OK).body(bookingDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id){
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
