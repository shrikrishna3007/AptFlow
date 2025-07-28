package com.project.aptflow.mapper;

import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class, RoomMapper.class, BillMapper.class})
public interface BookingMapper {
    @Mapping(source = "userEntity", target = "userDTO")
    @Mapping(source = "roomEntity", target = "roomDTO")
    @Mapping(source = "billEntity", target = "billDTO")
    BookingDTO entityToDTO(BookingEntity bookingEntity);

    @Mapping(source = "userDTO", target = "userEntity")
    @Mapping(source = "roomDTO", target = "roomEntity")
    @Mapping(source = "billDTO", target = "billEntity")
    BookingEntity dtoToEntity(BookingDTO bookingDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userEntity", ignore = true) // Prevent direct FK updates
    @Mapping(target = "roomEntity", ignore = true) // Prevent direct FK updates
    @Mapping(target = "billEntity", ignore = true) // Prevent direct FK updates
    void updateBooking(@MappingTarget BookingEntity booking, BookingDTO updatedBooking);
}
