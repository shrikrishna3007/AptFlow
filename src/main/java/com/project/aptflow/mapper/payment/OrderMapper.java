package com.project.aptflow.mapper.payment;

import com.project.aptflow.dto.payment.OrderDTO;
import com.project.aptflow.entity.payment.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
//    @Mapping(source = "GenerateBillEntity", target = "generateBillEntity")
    OrderDTO entityToDTO(OrderEntity orderEntity);

//    @Mapping(source = "generateBillEntity", target = "generateBillEntity")
    OrderEntity dtoToEntity(OrderDTO orderDTO);
}
