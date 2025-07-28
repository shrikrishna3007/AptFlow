package com.project.aptflow.unittest.controller;

import com.project.aptflow.controller.GenerateBillController;
import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.dto.billing.RoomSummaryDTO;
import com.project.aptflow.dto.billing.UserSummaryDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.service.GenerateBillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Generate Bill Controller Unit Test")
public class GenerateBillControllerUnitTest {
    @Mock
    private GenerateBillService generateBillService;

    @InjectMocks
    private GenerateBillController generateBillController;

    private GenerateBillDTO generateBillDTO;
    private List<GenerateBillDTO> generateBillDTOList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Bill DTO
        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);
        billDTO.setRoomNumber("RoomID-Test");
        billDTO.setMonth("2025-06");
        billDTO.setElectricityBill(new BigDecimal(1000));
        billDTO.setUnitPrice(new BigDecimal(2));
        billDTO.setRentPerDay(new BigDecimal(500));

        // Room Summary DTO
        RoomSummaryDTO roomSummaryDTO = new RoomSummaryDTO();
        roomSummaryDTO.setRoomNumber("RoomID-Test");
        roomSummaryDTO.setRoomCapacity(2);
        roomSummaryDTO.setRoomStatus(RoomStatus.OCCUPIED);
        roomSummaryDTO.setRoomRent(new BigDecimal(1000));
        roomSummaryDTO.setRoomSharing(true);

        // User Summary DTO
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO();
        userSummaryDTO.setAdhaarNumber("TestID");
        userSummaryDTO.setName("TestName");
        userSummaryDTO.setEmail("TestEmail");

        // Booking DTO
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setCheckIn(LocalDate.now());
        bookingDTO.setCheckOut(LocalDate.now().plusDays(1));
        bookingDTO.setState(BookingStatus.ACTIVE);
        bookingDTO.setCancelled(false);

        generateBillDTO = new GenerateBillDTO();
        generateBillDTO.setId(1L);
        generateBillDTO.setBillDTO(billDTO);
        generateBillDTO.setRoomSummaryDTO(roomSummaryDTO);
        generateBillDTO.setUserSummaryDTO(userSummaryDTO);
        generateBillDTO.setBookingDTO(bookingDTO);
        generateBillDTO.setTotal(new BigDecimal(0));
        generateBillDTO.setMonth(YearMonth.now());
        generateBillDTO.setDeliveryStatus(DeliveryStatus.NOT_SENT);
        generateBillDTO.setGenerateBillStatus(GenerateBillStatus.NOT_CALCULATED);

        generateBillDTOList = List.of(generateBillDTO);

    }

    @Nested
    @DisplayName("Get Bills Tests")
    class GetBillsTests {
        @Test
        @DisplayName("Get All Bills: Success")
        void getAllBills_SuccessTest() {
            // Arrange
            when(generateBillService.getAllBills()).thenReturn(generateBillDTOList);
            // Act
            ResponseEntity<List<GenerateBillDTO>> response = generateBillController.getAllBills();
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getBillDTO().getRoomNumber()).isEqualTo("RoomID-Test");
            // Verify
            verify(generateBillService, times(1)).getAllBills();
            verifyNoMoreInteractions(generateBillService);
        }

        @Test
        @DisplayName("Get All Bills: Empty List")
        void getAllBills_EmptyListTest() {
            // Arrange
            when(generateBillService.getAllBills()).thenReturn(List.of());
            // Act
            ResponseEntity<List<GenerateBillDTO>> response = generateBillController.getAllBills();
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(0);
            // Verify
            verify(generateBillService, times(1)).getAllBills();
            verifyNoMoreInteractions(generateBillService);
        }

        @Test
        @DisplayName("Get Bill By Id: Success")
        void getBillById_SuccessTest() {
            // Arrange
            when(generateBillService.getBillById(1L)).thenReturn(generateBillDTO);
            // Act
            ResponseEntity<GenerateBillDTO> response = generateBillController.getBillById(1L);
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getBillDTO().getRoomNumber()).isEqualTo("RoomID-Test");
            // Verify
            verify(generateBillService, times(1)).getBillById(1L);
            verifyNoMoreInteractions(generateBillService);
        }

        @Test
        @DisplayName("Get Bills By Adhaar Number: Success")
        void getBillsByAdhaarNumber_SuccessTest() {
            // Arrange
            when(generateBillService.getBillsByAdhaarNumber("TestID")).thenReturn(generateBillDTOList);
            // Act
            ResponseEntity<List<GenerateBillDTO>> response = generateBillController.getBillsByAdhaarNumber("TestID");
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getUserSummaryDTO().getAdhaarNumber()).isEqualTo("TestID");
            assertThat(response.getBody().get(0).getBillDTO().getRoomNumber()).isEqualTo("RoomID-Test");
            // Verify
            verify(generateBillService, times(1)).getBillsByAdhaarNumber("TestID");
            verifyNoMoreInteractions(generateBillService);
        }
    }

    @Nested
    @DisplayName("Update Bill Tests")
    class UpdateBillTests {
        @Test
        @DisplayName("Update Bill: Success")
        void updateBill_SuccessTest() {
            // Arrange
            when(generateBillService.updateBill(1L, generateBillDTO)).thenReturn(generateBillDTO);
            // Act
            ResponseEntity<ResponseDTO<GenerateBillDTO>> response = generateBillController.updateBill(1L, generateBillDTO);
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Data updated successfully...");
            assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.name());
            assertThat(response.getBody().getData()).isEqualTo(generateBillDTO);
            // Verify
            verify(generateBillService, times(1)).updateBill(1L, generateBillDTO);
            verifyNoMoreInteractions(generateBillService);
        }
    }

    @Nested
    @DisplayName("Delete Bill Tests")
    class DeleteBillTests {
        @Test
        @DisplayName("Delete Bill: Success")
        void deleteBill_SuccessTest() {
            // Arrange
            doNothing().when(generateBillService).deleteBill(1L);
            // Act
            ResponseEntity<Void> response = generateBillController.deleteBill(1L);
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            // Verify
            verify(generateBillService, times(1)).deleteBill(1L);
            verifyNoMoreInteractions(generateBillService);
        }
    }

    @Nested
    @DisplayName("Generate Bill Tests")
    class GenerateBillTests {
        @Test
        @DisplayName("Generate Bill: Success")
        void generateBill_SuccessTest() {
            // Arrange
            doNothing().when(generateBillService).generateBill(generateBillDTO);
            // Act
            ResponseEntity<MessageResponseDTO> response = generateBillController.generateBill(generateBillDTO);
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Bill generated successfully");
            // Verify
            verify(generateBillService, times(1)).generateBill(generateBillDTO);
            verifyNoMoreInteractions(generateBillService);
        }
    }
}
