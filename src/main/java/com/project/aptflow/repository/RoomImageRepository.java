package com.project.aptflow.repository;

import com.project.aptflow.entity.RoomImagesEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomImageRepository extends MongoRepository<RoomImagesEntity,String> {
    Optional<RoomImagesEntity> findByRoomNumber(String roomNumber);
}
