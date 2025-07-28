package com.project.aptflow.entity;

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
@Table(name = "Bill")
public class BillEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name="roomNumber", referencedColumnName = "roomNumber", nullable = false)
    private RoomEntity room;
    private String month;
    private BigDecimal electricityBill;
    private BigDecimal unitPrice;
    private BigDecimal rentPerDay;
}
