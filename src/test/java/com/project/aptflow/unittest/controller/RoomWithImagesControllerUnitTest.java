package com.project.aptflow.unittest.controller;

import com.project.aptflow.controller.RoomWithImagesController;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.service.RoomWithImagesService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("Room With Images Controller Unit Tests")
public class RoomWithImagesControllerUnitTest {
    @Mock
    private RoomWithImagesService roomWithImagesService;

    @InjectMocks
    private RoomWithImagesController roomWithImagesController;

    private RoomWithImagesDTO roomWithImagesDTO;
    private List<RoomWithImagesDTO> roomWithImagesDTOList;

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

        List<String> imageList = List.of("image1", "image2");
        roomWithImagesDTO = new RoomWithImagesDTO();
        roomWithImagesDTO.setRoomDTO(roomDTO);
        roomWithImagesDTO.setImageIDs(imageList);

        roomWithImagesDTOList = List.of(roomWithImagesDTO);
    }

    @Test
    void getRoomDetails_SuccessTest() {
        String roomNumber = "RoomID-Test";
        // Arrange
        when(roomWithImagesService.getRoomDetailsByRoomNumber(roomNumber)).thenReturn(roomWithImagesDTO);

        // Act
        ResponseEntity<RoomWithImagesDTO> response = roomWithImagesController.getRoomDetails(roomNumber);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roomWithImagesDTO);

        // Verify that service method was called
        verify(roomWithImagesService,times(1)).getRoomDetailsByRoomNumber(roomNumber);
        verifyNoMoreInteractions(roomWithImagesService);
    }

    @Test
    void getAllRoomDetails_SuccessTest() {
        // Arrange
        when(roomWithImagesService.getAllRoomDetails()).thenReturn(roomWithImagesDTOList);
        // Act
        ResponseEntity<List<RoomWithImagesDTO>> response = roomWithImagesController.getAllRoomDetails();
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRoomDTO().getRoomNumber()).isEqualTo("RoomID-Test");
        // Verify that service method was called
        verify(roomWithImagesService,times(1)).getAllRoomDetails();
        verifyNoMoreInteractions(roomWithImagesService);
    }

    @Test
    void getAvailableRooms_SuccessTest() {
        // Arrange: Call the service method
        when(roomWithImagesService.getAvailableRooms(RoomStatus.AVAILABLE)).thenReturn(roomWithImagesDTOList);
        // Act: Call the controller method
        ResponseEntity<List<RoomWithImagesDTO>> response = roomWithImagesController.getAvailableRooms(RoomStatus.AVAILABLE);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRoomDTO().getRoomStatus()).isEqualTo(RoomStatus.AVAILABLE);
        // Verify that service method was called
        verify(roomWithImagesService,times(1)).getAvailableRooms(RoomStatus.AVAILABLE);
        verifyNoMoreInteractions(roomWithImagesService);
    }

    @Test
    void getAvailableRooms_EmptyListTest() {
        // Arrange: Call the service method
        when(roomWithImagesService.getAvailableRooms(RoomStatus.AVAILABLE)).thenReturn(List.of());
        // Act: Call the controller method
        ResponseEntity<List<RoomWithImagesDTO>> response = roomWithImagesController.getAvailableRooms(RoomStatus.AVAILABLE);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        assertThat(response.getBody()).hasSize(0);
        // Verify that service method was called
        verify(roomWithImagesService,times(1)).getAvailableRooms(RoomStatus.AVAILABLE);
        verifyNoMoreInteractions(roomWithImagesService);
    }

    @Test
    void updateRoomDetailsWithImages_SuccessTest() {
        String roomNumber = "RoomID-Test";
        MultipartFile[] mockImages = {
                new MockMultipartFile("images", "test-image1.jpg", "image/jpeg", "dummy-img".getBytes())
        };
        // Arrange: Call the service method
        when(roomWithImagesService.updateRoomDetailsWithImages(eq(roomNumber),any(RoomWithImagesDTO.class),any(MultipartFile[].class)))
                .thenReturn(roomWithImagesDTO);
        // Act: Call the controller method
        ResponseEntity<ResponseDTO<RoomWithImagesDTO>> response = roomWithImagesController.updateRoomDetailsWithImages(roomNumber,roomWithImagesDTO,mockImages);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseDTO<RoomWithImagesDTO> responseDTO = response.getBody();
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getMessage()).isEqualTo("Data updated successfully...");
        assertThat(responseDTO.getStatus()).isEqualTo(HttpStatus.OK.name());
        assertThat(responseDTO.getData()).isNotNull();

        RoomDTO roomDTO = responseDTO.getData().getRoomDTO();
        assertThat(roomDTO.getRoomNumber()).isEqualTo("RoomID-Test");
        assertThat(roomDTO.getRoomType()).isEqualTo("Room-Type");
        assertThat(roomDTO.getRoomCapacity()).isEqualTo(2);
        assertThat(roomDTO.getRoomDescription()).isEqualTo("Room-Description");
        assertThat(roomDTO.getRoomStatus()).isEqualTo(RoomStatus.AVAILABLE);
        assertThat(roomDTO.getRoomRent()).isEqualTo(new BigDecimal(1000));
        assertThat(roomDTO.isRoomSharing()).isEqualTo(true);

        assertThat(responseDTO.getData().getImageIDs()).containsOnly("image1", "image2");

        // Verify that service method was called
        verify(roomWithImagesService,times(1)).updateRoomDetailsWithImages(eq(roomNumber),any(RoomWithImagesDTO.class),any(MultipartFile[].class));
        verifyNoMoreInteractions(roomWithImagesService);
    }
}
