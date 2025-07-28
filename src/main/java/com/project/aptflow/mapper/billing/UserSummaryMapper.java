package com.project.aptflow.mapper.billing;

import com.project.aptflow.dto.billing.UserSummaryDTO;
import com.project.aptflow.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSummaryMapper {
    UserSummaryDTO entityToDTO(UserEntity userEntity);
    UserEntity dtoToEntity(UserSummaryDTO userSummaryDTO);

}
