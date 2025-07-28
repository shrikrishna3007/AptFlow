package com.project.aptflow.service.payment.impl;

import com.project.aptflow.dto.payment.PaymentVerificationDTO;
import com.project.aptflow.entity.payment.OrderEntity;
import com.project.aptflow.enums.PaymentStatus;
import com.project.aptflow.exceptions.HmacGenerationException;
import com.project.aptflow.exceptions.PaymentVerificationException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.repository.payment.OrderRepository;
import com.project.aptflow.service.payment.PaymentService;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderRepository orderRepository;

    public PaymentServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void verifyPayment(PaymentVerificationDTO paymentVerificationDTO) {
        String data = paymentVerificationDTO.getRazorpayOrderId() + "|" + paymentVerificationDTO.getRazorpayPaymentId();
        String generatedSignature = generateHMAC(data, razorpayKeySecret);

        if (!generatedSignature.equals(paymentVerificationDTO.getRazorpaySignature())){
            throw new PaymentVerificationException("Invalid payment signature");
        }
        // Update Order Entity
        OrderEntity orderEntity = orderRepository.findByRazorpayOrderId(paymentVerificationDTO.getRazorpayOrderId())
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));
        orderEntity.setRazorpayPaymentId(paymentVerificationDTO.getRazorpayPaymentId());
        orderEntity.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(orderEntity);
        logger.info("Payment Verified Successfully. Order ID: {}, Payment Status: {}", orderEntity.getId(),orderEntity.getPaymentStatus());
    }

    private String generateHMAC(String data, String razorpayKeySecret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        }catch (Exception e) {
            logger.error("Error generating HMAC: {}", e.getMessage(), e);
            throw new HmacGenerationException("Error generating HMAC", e);
        }
    }
}
