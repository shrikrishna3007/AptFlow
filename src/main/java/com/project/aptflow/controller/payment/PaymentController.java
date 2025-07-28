package com.project.aptflow.controller.payment;

import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.payment.PaymentVerificationDTO;
import com.project.aptflow.service.payment.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/verify")
    public ResponseEntity<MessageResponseDTO> verifyPayment(PaymentVerificationDTO paymentVerificationDTO){
        paymentService.verifyPayment(paymentVerificationDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Payment verified successfully", HttpStatus.OK));
    }
}
