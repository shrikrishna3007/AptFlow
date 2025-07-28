package com.project.aptflow.entity;

import com.project.aptflow.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "adhaar_number",referencedColumnName = "adhaarNumber")
    private UserEntity userEntity;
    @ManyToOne
    @JoinColumn(name = "room_number",referencedColumnName = "roomNumber")
    private RoomEntity roomEntity;
    @ManyToOne
    @JoinColumn(name="bill_id",referencedColumnName = "id")
    private BillEntity billEntity;
    private LocalDate checkIn;
    private LocalDate checkOut;
    @Enumerated(EnumType.STRING)
    private BookingStatus state;
    @Column(nullable = false)
    private boolean cancelled = false;
}
