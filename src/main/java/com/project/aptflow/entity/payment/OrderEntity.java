package com.project.aptflow.entity.payment;

import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "bill_id", referencedColumnName = "id")
    private GenerateBillEntity generateBillEntity;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private String failureReason;
    private String razorpayOrderId;
    private String razorpayPaymentId;
}
