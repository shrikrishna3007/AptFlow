package com.project.aptflow.mapper;

import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.entity.RoomEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    RoomEntity dtoToEntity(RoomDTO roomDTO);
    RoomDTO entityToDto(RoomEntity roomEntity);
}
