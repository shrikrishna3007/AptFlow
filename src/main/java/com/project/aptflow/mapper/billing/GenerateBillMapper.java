package com.project.aptflow.mapper.billing;

import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.mapper.BookingMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        uses = {
                RoomSummaryMapper.class,
                UserSummaryMapper.class,
                BillMapper.class,
                BookingMapper.class
        }
)
public interface GenerateBillMapper {

    @Mapping(source = "billEntity", target = "billDTO")
    @Mapping(source = "roomEntity", target = "roomSummaryDTO")
    @Mapping(source = "userEntity", target = "userSummaryDTO")
    @Mapping(source = "bookingEntity", target = "bookingDTO")
    GenerateBillDTO entityToDTO(GenerateBillEntity generateBillEntity);

    @Mapping(source = "billDTO", target = "billEntity")
    @Mapping(source = "roomSummaryDTO", target = "roomEntity")
    @Mapping(source = "userSummaryDTO", target = "userEntity")
    @Mapping(source = "bookingDTO", target = "bookingEntity")
    GenerateBillEntity dtoToEntity(GenerateBillDTO generateBillDTO);

    @Mapping(target = "userEntity", ignore = true)
    @Mapping(target = "roomEntity", ignore = true)
    @Mapping(target = "billEntity", ignore = true)
    @Mapping(target = "bookingEntity", ignore = true)
    void updateBillEntity(@MappingTarget GenerateBillEntity bill, GenerateBillDTO updateBillDTO);
}
