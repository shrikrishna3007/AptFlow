package com.project.aptflow.repository.payment;

import com.project.aptflow.entity.payment.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByRazorpayOrderId(String razorpayOrderId);
}
