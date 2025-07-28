package com.project.aptflow.service.impl;

import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.RoomImagesEntity;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.exceptions.PersistenceException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.RoomWithImagesMapper;
import com.project.aptflow.repository.RoomImageRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.service.RoomWithImagesService;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomWithImagesServiceImpl implements RoomWithImagesService {
    private final RoomWithImagesMapper roomWithImagesMapper;
    private final RoomRepository roomRepository;
    private final RoomImageRepository imageRepository;
    private final GridFsTemplate gridFsTemplate;

    public RoomWithImagesServiceImpl(RoomWithImagesMapper roomWithImagesMapper, RoomRepository roomRepository, RoomImageRepository imageRepository, GridFsTemplate gridFsTemplate) {
        this.roomWithImagesMapper = roomWithImagesMapper;
        this.roomRepository = roomRepository;
        this.imageRepository = imageRepository;
        this.gridFsTemplate = gridFsTemplate;
    }

    private static final String MESSAGE= "Data not found...";

    @Override
    public RoomWithImagesDTO getRoomDetailsByRoomNumber(String roomNumber) {
        RoomEntity room = roomRepository.findById(roomNumber).orElseThrow(()-> new ResourceNotFoundException(MESSAGE));
        RoomImagesEntity imagesEntity=imageRepository.findByRoomNumber(roomNumber)
                .orElseThrow(()-> new ResourceNotFoundException(MESSAGE));
        return roomWithImagesMapper.entityToDTO(room,imagesEntity);
    }

    @Override
    public List<RoomWithImagesDTO> getAllRoomDetails() {
        List<RoomEntity> roomEntities = roomRepository.findAll();
        List<RoomWithImagesDTO> roomWithImagesDTOList =new ArrayList<>();

        for (RoomEntity room: roomEntities){
            /*
            Fetch room images based on room number for each room record.
             */
            RoomImagesEntity imagesEntity = imageRepository.findByRoomNumber(room.getRoomNumber())
                    .orElseThrow(()->new ResourceNotFoundException(MESSAGE));
            /*
            Map room entity and images entity to DTO
             */
            RoomWithImagesDTO roomWithImagesDTO = roomWithImagesMapper.entityToDTO(room, imagesEntity);
            roomWithImagesDTOList.add(roomWithImagesDTO);
        }
        return roomWithImagesDTOList;
    }

    @Override
    public List<RoomWithImagesDTO> getAvailableRooms(RoomStatus roomStatus) {
        /*
        Find rooms by status
         */
        List<RoomEntity> rooms = roomRepository.findByRoomStatus(roomStatus);
        if(rooms .isEmpty()){
            throw new ResourceNotFoundException("No available rooms found...");
        }
        List<RoomWithImagesDTO> roomWithImagesDTOList=new ArrayList<>();
        /*
        Based on room find images for each available room.
         */
        for (RoomEntity room: rooms){
            RoomImagesEntity imagesEntity =imageRepository.findByRoomNumber(room.getRoomNumber()).orElseThrow(()->new ResourceNotFoundException("No images found...."));
            RoomWithImagesDTO roomWithImagesDTO =roomWithImagesMapper.entityToDTO(room,imagesEntity);
            roomWithImagesDTOList.add(roomWithImagesDTO);
        }
        return roomWithImagesDTOList;
    }

    @Override
    public RoomWithImagesDTO updateRoomDetailsWithImages(String roomNumber, RoomWithImagesDTO updatedRoomDetails, MultipartFile[] newImages) {
        RoomEntity room =roomRepository.findById(roomNumber).orElseThrow(()->new ResourceNotFoundException(MESSAGE));

        RoomImagesEntity roomImagesEntity = imageRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room images with room number not found"));
        roomWithImagesMapper.updateRoomEntity(room,updatedRoomDetails);
        roomRepository.save(room);

        // Delete existing images
        Optional.ofNullable(roomImagesEntity.getImageIDs())
                .ifPresent(ids->ids.forEach(id->gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)))));
        // Save new images
        List<String> newImageIDs = new ArrayList<>();
        try {
            for (MultipartFile file: newImages){
                String fileName = "image_"+ roomNumber+"_"+ UUID.randomUUID();
                ObjectId objectId=gridFsTemplate.store(file.getInputStream(),fileName);
                newImageIDs.add(objectId.toString());
            }
            /*
            Update images with new images
             */
            roomImagesEntity.setImageIDs(newImageIDs);
            imageRepository.save(roomImagesEntity);
        }catch (IOException e){
            throw new PersistenceException("Error storing images..."+e.getMessage());
        }
        return roomWithImagesMapper.entityToDTO(room,roomImagesEntity);
    }
}
