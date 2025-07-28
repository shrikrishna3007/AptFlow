package com.project.aptflow.repository;

import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Repository
public interface GenerateBillRepository extends JpaRepository<GenerateBillEntity, Long> {
    @Query("SELECT gb FROM GenerateBillEntity gb JOIN gb.userEntity u WHERE u.adhaarNumber = :adhaarNumber")
    List<GenerateBillEntity> findBillsByAdhaarNumber(@Param("adhaarNumber") String adhaarNumber);

    @Query("SELECT b FROM GenerateBillEntity b WHERE b.month=:month AND b.deliveryStatus=:status")
    List<GenerateBillEntity> findByMonthAndStatus(@Param("month") YearMonth month, @Param("status") DeliveryStatus deliveryStatus);

    @Query("SELECT b FROM GenerateBillEntity b WHERE b.bookingEntity.checkOut=:checkOutDate AND b.deliveryStatus=:deliveryStatus")
    List<GenerateBillEntity> findByCheckOutDate(@Param("checkOutDate") LocalDate checkOutDate, @Param("deliveryStatus")DeliveryStatus deliveryStatus);
}
