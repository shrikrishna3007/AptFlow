package com.project.aptflow.service;

import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.enums.RoomStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RoomWithImagesService {
    RoomWithImagesDTO getRoomDetailsByRoomNumber(String roomNumber);

    List<RoomWithImagesDTO> getAllRoomDetails();

    List<RoomWithImagesDTO> getAvailableRooms(RoomStatus roomStatus);

    RoomWithImagesDTO updateRoomDetailsWithImages(String roomNumber, RoomWithImagesDTO updatedRoomDetails, MultipartFile[] newImages);
}
