package com.project.aptflow.mapper.billing;

import com.project.aptflow.dto.billing.RoomSummaryDTO;
import com.project.aptflow.entity.RoomEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomSummaryMapper {
    RoomSummaryDTO entityToDTO(RoomEntity roomEntity);
    RoomEntity dtoToEntity(RoomSummaryDTO roomSummaryDTO);
}
