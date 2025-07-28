package com.project.aptflow.mapper;

import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // Map UserDTO to UserEntity
    @Mapping(target = "password", ignore = true)
    UserEntity dtoToEntity(UserDTO userDTO);

    //Map entity to dto
    UserDTO entityToDTO(UserEntity userEntity);

    @Mapping(target = "password", ignore = true)
    void updateUserEntity(@MappingTarget UserEntity user, UserDTO updateUser);
}
