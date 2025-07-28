package com.project.aptflow.service;

import com.project.aptflow.dto.RoomManagementDTO;
import org.springframework.web.multipart.MultipartFile;

public interface RoomManagementService {
    void addRoomWithImages(RoomManagementDTO roomManagementDTO, MultipartFile[] images);
    void deleteRoomWithImages(String roomNumber);
}
