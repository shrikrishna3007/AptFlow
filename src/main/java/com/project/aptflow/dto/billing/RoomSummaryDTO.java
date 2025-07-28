package com.project.aptflow.dto.billing;

import com.project.aptflow.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomSummaryDTO {
    private String roomNumber;
    private BigDecimal roomRent;
    private RoomStatus roomStatus;
    private boolean roomSharing;
    private int roomCapacity;

}
