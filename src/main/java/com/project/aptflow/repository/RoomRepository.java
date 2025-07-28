package com.project.aptflow.repository;

import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity,String> {
    List<RoomEntity> findByRoomStatus(RoomStatus roomStatus);
}
