package com.project.aptflow.dto;

import com.project.aptflow.enums.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class RoomDTO {
    private String roomNumber;
    private String roomType;
    private String roomDescription;
    private BigDecimal roomRent;
    private RoomStatus roomStatus;
    private boolean roomSharing;
    private int roomCapacity;
}
