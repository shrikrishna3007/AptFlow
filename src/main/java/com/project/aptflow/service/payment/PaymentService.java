package com.project.aptflow.service.payment;

import com.project.aptflow.dto.payment.PaymentVerificationDTO;

public interface PaymentService {
    void verifyPayment(PaymentVerificationDTO paymentVerificationDTO);
}
