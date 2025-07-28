package com.project.aptflow.controller.payment;

import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.service.payment.RazorpayWebHookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/webhook")
public class RazorpayWebHookController {
    private final RazorpayWebHookService razorpayWebHookService;

    public RazorpayWebHookController(RazorpayWebHookService razorpayWebHookService) {
        this.razorpayWebHookService = razorpayWebHookService;
    }

    @PostMapping("/razorpay")
    public ResponseEntity<MessageResponseDTO> handleRazorpayWebhook(HttpServletRequest request) {
        try {
            String payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            String signature = request.getHeader("X-Razorpay-Signature");

            if (!razorpayWebHookService.verifySignature(payload, signature)) {
                return ResponseEntity.badRequest().body(new MessageResponseDTO("Invalid signature", HttpStatus.BAD_REQUEST));
            }

            razorpayWebHookService.processEvent(payload);
            return ResponseEntity.ok(new MessageResponseDTO("Webhook processed successfully", HttpStatus.OK));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO("Error processing webhook", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
