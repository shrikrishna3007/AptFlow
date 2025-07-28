package com.project.aptflow.controller;

import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.RoomManagementDTO;
import com.project.aptflow.service.RoomManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/room-management")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RoomManagementController {
    private final RoomManagementService roomManagementService;

    public RoomManagementController(RoomManagementService roomManagementService) {
        this.roomManagementService = roomManagementService;
    }

    /**
     * This function is responsible for adding a new room along with its associated images to the system.
     *
     * @param roomManagementDTO The DTO containing the room details and bill details.
     * @param images An array of MultipartFile objects representing the images to be associated with the room.
     *
     * @return A ResponseEntity object containing a MessageResponseDTO with a success message and HTTP status code CREATED.
     *         If the room is successfully added, the MessageResponseDTO will contain the message "Data is saved...".
     */
    @PostMapping(consumes = {"multipart/form-data"}, produces = "application/json")
    public ResponseEntity<MessageResponseDTO> addRoomWithImages(@RequestPart("roomManagementDTO") RoomManagementDTO roomManagementDTO, @RequestParam("images") MultipartFile[] images) {
        roomManagementService.addRoomWithImages(roomManagementDTO,images);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDTO("Data saved successfully...",HttpStatus.CREATED));
    }

    /**
     * This function is responsible for deleting a room along with its associated images from the system.
     *
     * @param roomNumber The unique identifier of the room to be deleted. This parameter is obtained from the URL path variable.
     *
     * @return A ResponseEntity object containing a MessageResponseDTO with a success message and HTTP status code OK.
     *         If the room is successfully deleted, the MessageResponseDTO will contain the message "Data deleted successfully".
     */
    @DeleteMapping("/{roomNumber}")
    public ResponseEntity<Void> deleteRoomWithImages(@PathVariable String roomNumber){
        roomManagementService.deleteRoomWithImages(roomNumber);
        return ResponseEntity.noContent().build();
    }
}
