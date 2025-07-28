package com.project.aptflow.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentVerificationDTO {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
