package com.project.aptflow.controller.publicapi;

import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.service.RoomWithImagesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/rooms")
public class PublicRoomController {
    private final RoomWithImagesService roomWithImagesService;

    public PublicRoomController(RoomWithImagesService roomWithImagesService) {
        this.roomWithImagesService = roomWithImagesService;
    }

    /*
    This is for user. Without sign up, can view the available rooms.
     */
    @GetMapping("/available")
    public ResponseEntity<List<RoomWithImagesDTO>> getPublicAvailableRooms(){
        List<RoomWithImagesDTO> roomWithImagesDTOList= roomWithImagesService.getAvailableRooms(RoomStatus.AVAILABLE);
        return ResponseEntity.status(HttpStatus.OK).body(roomWithImagesDTOList);
    }
}
