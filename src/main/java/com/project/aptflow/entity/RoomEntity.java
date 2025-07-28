package com.project.aptflow.entity;

import com.project.aptflow.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Room")
public class RoomEntity {
    @Id
    private String roomNumber;
    private String roomType;
    private String roomDescription;
    private BigDecimal roomRent;
    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;
    private boolean roomSharing;
    private int roomCapacity;
    // Cascading ensures that operations on RoomEntity propagate to BillEntity.
    // orphanRemoval=true deletes the BillEntity if it becomes disassociated.
    @OneToOne(mappedBy = "room",cascade = CascadeType.ALL, orphanRemoval = true)
    private BillEntity bill;
}
