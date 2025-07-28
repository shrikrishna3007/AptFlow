package com.project.aptflow.service.impl;

import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.UserMapper;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userDTOList.add(userMapper.entityToDTO(userEntity));
        }
        return userDTOList;
    }

    @Override
    public UserDTO getUserDetails(String adhaarNumber) {
        UserEntity user = userRepository.findById(adhaarNumber).orElseThrow(()->new ResourceNotFoundException("User not found: "));
        return userMapper.entityToDTO(user);
    }

    @Override
    public UserDTO updateUser(String adhaarNumber, UserDTO updateUser) {
        UserEntity user = userRepository.findById(adhaarNumber).orElseThrow(()->new ResourceNotFoundException("User not found"));
        userMapper.updateUserEntity(user, updateUser);
        return userMapper.entityToDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(String adhaarNumber) {
        if (!userRepository.existsById(adhaarNumber)){
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(adhaarNumber);
    }
}
