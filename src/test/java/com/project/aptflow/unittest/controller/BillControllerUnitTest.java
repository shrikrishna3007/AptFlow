package com.project.aptflow.unittest.controller;

import com.project.aptflow.controller.BillController;
import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.service.BillService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Bill Controller Unit Tests")
public class BillControllerUnitTest {
    @Mock
    private BillService billService;

    @InjectMocks
    private BillController billController;

    private BillDTO billDTO;
    private List<BillDTO> billDTOList;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        billDTO = new BillDTO();
        billDTO.setId(1L);
        billDTO.setRoomNumber("RoomID-Test");
        billDTO.setMonth("2025-06");
        billDTO.setElectricityBill(new BigDecimal(1000));
        billDTO.setUnitPrice(new BigDecimal(2));
        billDTO.setRentPerDay(new BigDecimal(500));
        billDTOList = List.of(billDTO);
    }

    @Nested
    @DisplayName("Get Bill Tests")
    class GetBillTests{
        @Test
        @DisplayName("Get All Bills")
        void getAllBills_SuccessTest(){
            // Arrange
            when(billService.getAllBills()).thenReturn(billDTOList);
            // Act
            ResponseEntity<List<BillDTO>> response = billController.getAllBills();
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getRoomNumber()).isEqualTo("RoomID-Test");
            // Verify
            verify(billService,times(1)).getAllBills();
            verifyNoMoreInteractions(billService);
        }

        @Test
        @DisplayName("Get Bill By Id")
        void getBillById_SuccessTest(){
            Long id = 1L;
            // Arrange
            when(billService.getBillById(id)).thenReturn(billDTO);
            // Act
            ResponseEntity<BillDTO> response = billController.getBillById(id);
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getRoomNumber()).isEqualTo("RoomID-Test");
            // Verify
            verify(billService,times(1)).getBillById(id);
            verifyNoMoreInteractions(billService);
        }
    }

    @Nested
    @DisplayName("Update Bill Test")
    class UpdateBillTests{
        @Test
        @DisplayName("Update Bill")
        void updateBill_SuccessTest(){
            Long id = 1L;
            // Arrange
            when(billService.updateBill(id,billDTO)).thenReturn(billDTO);
            // Act
            ResponseEntity<ResponseDTO<BillDTO>> response = billController.updateBill(id,billDTO);
            ResponseDTO<BillDTO> responseDTO = response.getBody();
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseDTO).isNotNull();
            assertThat(responseDTO.getMessage()).isEqualTo("Bill updated successfully");
            assertThat(responseDTO.getStatus()).isEqualTo(HttpStatus.OK.name());
            assertThat(responseDTO.getData()).isEqualTo(billDTO);
            // Verify
            verify(billService,times(1)).updateBill(id,billDTO);
            verifyNoMoreInteractions(billService);
        }
    }
}
