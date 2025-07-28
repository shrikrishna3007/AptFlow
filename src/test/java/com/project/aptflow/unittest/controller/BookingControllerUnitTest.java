package com.project.aptflow.unittest.controller;

import com.project.aptflow.controller.BookingController;
import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.dto.booking.UserBookingUpdateDTO;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.enums.Role;
import com.project.aptflow.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Booking Controller Tests")
public class BookingControllerUnitTest {
    @Mock
    private BookingService bookingService;


    @InjectMocks
    private BookingController bookingController;
    private BookingDTO bookingDTO;

    private List<BookingDTO> bookingDTOList;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("TestRoomID1");
        UserDTO userDTO = new UserDTO("Test-Number", "Name-Test", "Test-address", "Gender-Test", "Mobile-Test", "Email-Test", Role.ROLE_USER);
        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);

        bookingDTO = new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setUserDTO(userDTO);
        bookingDTO.setRoomDTO(roomDTO);
        bookingDTO.setBillDTO(billDTO);
        bookingDTO.setCheckIn(LocalDate.now());
        bookingDTO.setCheckOut(LocalDate.now().plusDays(1));
        bookingDTO.setState(BookingStatus.ACTIVE);
        bookingDTO.setCancelled(false);

        bookingDTOList = List.of(bookingDTO);
    }

    @Test
    void roomBook_SuccessTest(){
        // Arrange
        doNothing().when(bookingService).roomBook(bookingDTO);
        // Act
        ResponseEntity<MessageResponseDTO> response = bookingController.roomBook(bookingDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Room is booked successfully");
        // Verify
        verify(bookingService,times(1)).roomBook(bookingDTO);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getAllBookings_SuccessTest(){
        // Mock the service method
        when(bookingService.getAllBookings()).thenReturn(bookingDTOList);
        ResponseEntity<List<BookingDTO>> response = bookingController.getAllBookings();
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRoomDTO().getRoomNumber()).isEqualTo("TestRoomID1");
        // Verify that service method was called
        verify(bookingService,times(1)).getAllBookings();
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void updateBooking_SuccessTest(){
        Long id = 1L;
        // Mock the service method
        when(bookingService.updateBooking(eq(id),any(BookingDTO.class))).thenReturn(bookingDTO);
        // Call controller method
        ResponseEntity<ResponseDTO<BookingDTO>> response = bookingController.updateBooking(id,bookingDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(bookingDTO);
        assertThat(response.getBody().getMessage()).isEqualTo("Data updated successfully...");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.name());
        // Verify that service method was called
        verify(bookingService,times(1)).updateBooking(eq(id),any(BookingDTO.class));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void updateCheckOutDate_SuccessTest(){
        UserBookingUpdateDTO userBookingUpdateDTO = new UserBookingUpdateDTO();
        userBookingUpdateDTO.setCheckOut(LocalDate.now().plusDays(3));

        Long id = 1L;
        bookingDTO.setCheckOut(userBookingUpdateDTO.getCheckOut());
        // Mock the service method
        when(bookingService.updateCheckOutDate(eq(id),eq(userBookingUpdateDTO.getCheckOut()))).thenReturn(bookingDTO);
        // Call controller method
        ResponseEntity<ResponseDTO<BookingDTO>> response = bookingController.updateCheckOutDate(id,userBookingUpdateDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(bookingDTO);
        assertThat(response.getBody().getMessage()).isEqualTo("Data updated successfully...");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.name());
        // Verify that service method was called
        verify(bookingService,times(1)).updateCheckOutDate(eq(id),eq(userBookingUpdateDTO.getCheckOut()));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void cancelBooking_SuccessTest(){
        Long id = 1L;
        // Mock the service method
        doNothing().when(bookingService).cancelBooking(eq(id));
        // Call controller method
        ResponseEntity<MessageResponseDTO> response = bookingController.cancelBooking(id);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Booking canceled successfully...");
        // Verify that service method was called
        verify(bookingService,times(1)).cancelBooking(eq(id));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getBookingById_SuccessTest(){
        Long id = 1L;
        // Mock the service method
        when(bookingService.getBookingById(id)).thenReturn(bookingDTO);
        // Call controller method
        ResponseEntity<BookingDTO> response = bookingController.getBookingById(id);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(bookingDTO);
        // Verify that service method was called
        verify(bookingService,times(1)).getBookingById(id);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void deleteBooking_SuccessTest(){
        Long id = 1L;
        // Mock the service method
        doNothing().when(bookingService).deleteBooking(id);
        // Call controller method
        ResponseEntity<Void> response = bookingController.deleteBooking(id);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        // Verify that service method was called
        verify(bookingService,times(1)).deleteBooking(id);
        verifyNoMoreInteractions(bookingService);
    }
}
