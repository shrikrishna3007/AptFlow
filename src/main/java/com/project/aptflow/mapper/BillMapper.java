package com.project.aptflow.mapper;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.entity.BillEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BillMapper {
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    BillDTO entityToDTO(BillEntity billEntity);

    @Mapping(target = "room.roomNumber", source = "roomNumber")
    BillEntity dtoToEntity(BillDTO billDTO);

    @Mapping(target = "room", ignore = true)
    void updateBillEntity(@MappingTarget BillEntity bill, BillDTO updateBillDTO);
}
