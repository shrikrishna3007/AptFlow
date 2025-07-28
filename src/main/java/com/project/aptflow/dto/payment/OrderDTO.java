package com.project.aptflow.dto.payment;

import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderDTO {
    private Long id;
    private GenerateBillEntity generateBillEntity;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String razorpayOrderId;
}
