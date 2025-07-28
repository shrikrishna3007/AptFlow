package com.project.aptflow.mapper;

import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.RoomImagesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface RoomWithImagesMapper {

    @Mapping(target = "roomDTO", source = "roomEntity")
    @Mapping(target = "imageIDs", source = "imagesEntity.imageIDs")
    RoomWithImagesDTO entityToDTO(RoomEntity roomEntity, RoomImagesEntity imagesEntity);

    @Mapping(target = "roomNumber", source = "dto.roomDTO.roomNumber")
    @Mapping(target = "roomType", source = "dto.roomDTO.roomType")
    @Mapping(target = "roomDescription", source = "dto.roomDTO.roomDescription")
    @Mapping(target = "roomStatus", source = "dto.roomDTO.roomStatus")
    @Mapping(target = "roomRent", source = "dto.roomDTO.roomRent")
    @Mapping(target = "roomSharing", source = "dto.roomDTO.roomSharing")
    @Mapping(target = "roomCapacity", source = "dto.roomDTO.roomCapacity")
    @Mapping(target = "bill", ignore = true)
    RoomEntity dtoToEntity(RoomWithImagesDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomNumber", source = "dto.roomDTO.roomNumber")
    @Mapping(target = "imageIDs", source = "imageIDs")  // Mapping List<String> from DTO to RoomImagesEntity
    RoomImagesEntity dtoToImageEntity(RoomWithImagesDTO dto, List<String> imageIDs);

    @Mapping(target = "roomNumber", source = "updatedRoomDetails.roomDTO.roomNumber")
    @Mapping(target = "roomType", source = "updatedRoomDetails.roomDTO.roomType")
    @Mapping(target = "roomDescription", source = "updatedRoomDetails.roomDTO.roomDescription")
    @Mapping(target = "roomRent", source = "updatedRoomDetails.roomDTO.roomRent")
    @Mapping(target = "roomStatus", source = "updatedRoomDetails.roomDTO.roomStatus")
    @Mapping(target = "roomSharing", source = "updatedRoomDetails.roomDTO.roomSharing")
    @Mapping(target = "roomCapacity", source = "updatedRoomDetails.roomDTO.roomCapacity")
    @Mapping(target = "bill", ignore = true) // Ignore if bill should not be updated
    void updateRoomEntity(@MappingTarget RoomEntity roomEntity, RoomWithImagesDTO updatedRoomDetails);

}
