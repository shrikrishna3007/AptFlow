package com.project.aptflow.unittest.controller.publicapi;

import com.project.aptflow.controller.publicapi.PublicRoomController;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.service.RoomWithImagesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicRoomControllerUnitTest {
    @Mock
    private RoomWithImagesService roomWithImagesService;
    @InjectMocks
    private PublicRoomController publicRoomController;

    @Test
    @DisplayName("Should Get Available Rooms: Public API Test")
    void getPublicAvailableRooms_SuccessTest() {
        // Arrange
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("RoomID-Test");
        roomDTO.setRoomStatus(RoomStatus.AVAILABLE);

        RoomWithImagesDTO roomWithImagesDTO = new RoomWithImagesDTO();
        roomWithImagesDTO.setRoomDTO(roomDTO);
        roomWithImagesDTO.setImageIDs(List.of("image1", "image2"));
        when(roomWithImagesService.getAvailableRooms(RoomStatus.AVAILABLE)).thenReturn(List.of(roomWithImagesDTO));
        // Act
        ResponseEntity<List<RoomWithImagesDTO>> response = publicRoomController.getPublicAvailableRooms();
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRoomDTO().getRoomNumber()).isEqualTo("RoomID-Test");
        // Verify
        verify(roomWithImagesService).getAvailableRooms(RoomStatus.AVAILABLE);
    }
}
