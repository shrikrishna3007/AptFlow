package com.project.aptflow.unittest.controller;

import com.project.aptflow.controller.RoomManagementController;
import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.RoomManagementDTO;
import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.service.RoomManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Room Management Controller Unit Test")
public class RoomManagementControllerUnitTest {

    @Mock
    private RoomManagementService roomManagementService;

    @InjectMocks
    private RoomManagementController roomManagementController;

    private RoomManagementDTO roomManagementDTO;
    private MultipartFile[] images;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("RoomID-Test");
        roomDTO.setRoomType("Room-Type");
        roomDTO.setRoomCapacity(2);
        roomDTO.setRoomDescription("Room-Description");
        roomDTO.setRoomStatus(RoomStatus.AVAILABLE);
        roomDTO.setRoomRent(new BigDecimal(1000));
        roomDTO.setRoomSharing(true);

        // Sample BillDTO
        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);
        billDTO.setRoomNumber("RoomID-Test");
        billDTO.setMonth("2025-06");
        billDTO.setElectricityBill(new BigDecimal(1000));
        billDTO.setUnitPrice(new BigDecimal(2));
        billDTO.setRentPerDay(new BigDecimal(500));

        RoomWithImagesDTO roomWithImagesDTO = new RoomWithImagesDTO();
        roomWithImagesDTO.setRoomDTO(roomDTO);

        // RoomManagementDTO data.
        roomManagementDTO = new RoomManagementDTO();
        roomManagementDTO.setRoomWithImagesDTO(roomWithImagesDTO);
        roomManagementDTO.setBillDTO(billDTO);
        // Sample images
        images = new MultipartFile[]{
            new MockMultipartFile("images", "room1.jpg", "image/jpeg", "dummy1".getBytes()),
            new MockMultipartFile("images", "room2.jpg", "image/jpeg", "dummy2".getBytes())
        };
    }

    @Test
    void addRoomWithImages_SuccessTest(){
        // Arrange
        doNothing().when(roomManagementService).addRoomWithImages(roomManagementDTO, images);
        // Act
        ResponseEntity<MessageResponseDTO> response = roomManagementController.addRoomWithImages(roomManagementDTO, images);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        MessageResponseDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Data saved successfully...", responseBody.getMessage());
        assertEquals(HttpStatus.CREATED.name(), responseBody.getStatus());
        assertNotNull(responseBody.getTimestamp());
        assertTrue(responseBody.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(responseBody.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));

        // Verify mocked service method called
        verify(roomManagementService,times(1)).addRoomWithImages(roomManagementDTO, images);
    }

    @Test
    void deleteRoomWithImages_SuccessTest(){
        // Arrange
        doNothing().when(roomManagementService).deleteRoomWithImages("RoomID-Test");
        // Act
        ResponseEntity<Void> response = roomManagementController.deleteRoomWithImages("RoomID-Test");
        // Assert statements
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
        assertNull(response.getBody());
        // Verify that service method was called
        verify(roomManagementService,times(1)).deleteRoomWithImages("RoomID-Test");
    }
}
