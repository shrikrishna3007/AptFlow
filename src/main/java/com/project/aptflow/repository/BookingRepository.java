package com.project.aptflow.repository;

import com.project.aptflow.entity.BookingEntity;
import com.project.aptflow.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity,Long> {
    // For cancelling the booking.
    Optional<BookingEntity> findByIdAndCancelledFalse(Long id);

    List<BookingEntity> findByCheckOut(LocalDate checkOut);

    List<BookingEntity> findByState(BookingStatus bookingStatus);
}
