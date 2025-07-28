package com.project.aptflow.entity;

import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class GenerateBillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "bill_id", referencedColumnName = "id")
    private BillEntity billEntity;
    @ManyToOne
    @JoinColumn(name="room_number",referencedColumnName = "roomNumber")
    private RoomEntity roomEntity;
    @ManyToOne
    @JoinColumn(name="adhaar_number",referencedColumnName = "adhaarNumber")
    private UserEntity userEntity;
    @ManyToOne
    @JoinColumn(name="booking_id", referencedColumnName = "id")
    private BookingEntity bookingEntity;
    private YearMonth month;
    private BigDecimal total = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
    @Enumerated(EnumType.STRING)
    private GenerateBillStatus generateBillStatus;

}
