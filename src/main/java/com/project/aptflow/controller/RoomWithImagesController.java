package com.project.aptflow.controller;

import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.service.RoomWithImagesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/room-with-images")
public class RoomWithImagesController {
    private final RoomWithImagesService roomWithImagesService;


    public RoomWithImagesController(RoomWithImagesService roomWithImagesService) {
        this.roomWithImagesService = roomWithImagesService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{roomNumber}")
    public ResponseEntity<RoomWithImagesDTO> getRoomDetails(@PathVariable String roomNumber){
        RoomWithImagesDTO roomWithImagesDTO= roomWithImagesService.getRoomDetailsByRoomNumber(roomNumber);
        return ResponseEntity.status(HttpStatus.OK).body(roomWithImagesDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<List<RoomWithImagesDTO>> getAllRoomDetails(){
        List<RoomWithImagesDTO> roomWithImagesDTOList= roomWithImagesService.getAllRoomDetails();
        return ResponseEntity.status(HttpStatus.OK).body(roomWithImagesDTOList);
    }

    /*
    This method is for admin control. Admin can access different status rooms
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomWithImagesDTO>> getAvailableRooms(@RequestParam RoomStatus roomStatus){
        List<RoomWithImagesDTO> roomWithImagesDTOList= roomWithImagesService.getAvailableRooms(roomStatus);
        return ResponseEntity.status(HttpStatus.OK).body(roomWithImagesDTOList);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{roomNumber}")
    public ResponseEntity<ResponseDTO<RoomWithImagesDTO>> updateRoomDetailsWithImages(@PathVariable String roomNumber, @RequestPart("roomDetails") RoomWithImagesDTO updatedRoomDetails, @RequestPart("images") MultipartFile[] newImages){
        RoomWithImagesDTO updatedDTO= roomWithImagesService.updateRoomDetailsWithImages(roomNumber,updatedRoomDetails,newImages);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Data updated successfully...",HttpStatus.OK,updatedDTO));
    }
}
