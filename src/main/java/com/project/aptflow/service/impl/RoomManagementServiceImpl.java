package com.project.aptflow.service.impl;

import com.project.aptflow.dto.RoomManagementDTO;
import com.project.aptflow.entity.BillEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.RoomImagesEntity;
import com.project.aptflow.exceptions.BadRequestException;
import com.project.aptflow.exceptions.PersistenceException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.mapper.RoomWithImagesMapper;
import com.project.aptflow.repository.BillRepository;
import com.project.aptflow.repository.RoomImageRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.service.RoomManagementService;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoomManagementServiceImpl implements RoomManagementService {
    private final RoomRepository roomRepository;
    private final RoomImageRepository imageRepository;
    private final BillRepository billRepository;
    private final RoomWithImagesMapper roomWithImagesMapper;
    private final GridFsTemplate gridFsTemplate;
    private final BillMapper billMapper;

    public RoomManagementServiceImpl(RoomRepository roomRepository, RoomImageRepository imageRepository, BillRepository billRepository, RoomWithImagesMapper roomWithImagesMapper, GridFsTemplate gridFsTemplate, BillMapper billMapper) {
        this.roomRepository = roomRepository;
        this.imageRepository = imageRepository;
        this.billRepository = billRepository;
        this.roomWithImagesMapper = roomWithImagesMapper;
        this.gridFsTemplate = gridFsTemplate;
        this.billMapper = billMapper;
    }

    public void validateData(RoomManagementDTO roomManagementDTO, MultipartFile[] images){
        if(roomManagementDTO.getRoomWithImagesDTO()==null){
            throw new BadRequestException("Room details must be provided");
        }
        if (roomManagementDTO.getBillDTO()==null){
            throw new BadRequestException("Bill details must be provided");
        }
        if( images == null || images.length ==0){
            throw new BadRequestException("Images must be provided");
        }
    }
    @Override
    public void addRoomWithImages(RoomManagementDTO roomManagementDTO, MultipartFile[] images) {
        validateData(roomManagementDTO, images);

        RoomEntity room = roomWithImagesMapper.dtoToEntity(roomManagementDTO.getRoomWithImagesDTO());
        roomRepository.save(room);

        BillEntity bill =billMapper.dtoToEntity(roomManagementDTO.getBillDTO());
        bill.setRoom(room);
        billRepository.save(bill);

        List<String> imageIDs= new ArrayList<>();
        for (MultipartFile image: images){
            try {
                String fileName= "room_"+roomManagementDTO.getRoomWithImagesDTO().getRoomDTO().getRoomNumber()+"_"+ UUID.randomUUID();
                ObjectId objectId= gridFsTemplate.store(image.getInputStream(),fileName);
                imageIDs.add(objectId.toString());
            }catch (IOException e){
                throw new PersistenceException("Error storing images.",e);
            }
        }

        RoomImagesEntity imagesEntity =roomWithImagesMapper.dtoToImageEntity(roomManagementDTO.getRoomWithImagesDTO(),imageIDs);
        imageRepository.save(imagesEntity);
    }

    @Override
    public void deleteRoomWithImages(String roomNumber) {
        RoomEntity room = roomRepository.findById(roomNumber).orElseThrow(()-> new ResourceNotFoundException("Room not found..."));

        RoomImagesEntity imagesEntity = imageRepository.findByRoomNumber(roomNumber).orElseThrow(()-> new ResourceNotFoundException("Images not found..."));

        if(imagesEntity!=null){
            for (String imageID: imagesEntity.getImageIDs()){
                gridFsTemplate.delete(new Query(Criteria.where("_id").is(imageID)));
            }
            imageRepository.delete(imagesEntity);
        }
        roomRepository.delete(room);
    }
}
