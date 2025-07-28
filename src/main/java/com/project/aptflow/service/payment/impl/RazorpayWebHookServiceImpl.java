package com.project.aptflow.service.payment.impl;

import com.project.aptflow.entity.payment.OrderEntity;
import com.project.aptflow.enums.PaymentStatus;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.repository.payment.OrderRepository;
import com.project.aptflow.service.payment.RazorpayWebHookService;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
public class RazorpayWebHookServiceImpl implements RazorpayWebHookService {
    private static final Logger logger = LoggerFactory.getLogger(RazorpayWebHookServiceImpl.class);
    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final OrderRepository orderRepository;

    public RazorpayWebHookServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Override
    public boolean verifySignature(String payload, String signature) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = Hex.encodeHexString(hash);
            return generatedSignature.equals(signature);
        }catch (Exception e){
            logger.error("Error generating HMAC: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void processEvent(String payload) {
        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        JSONObject entity = event.getJSONObject("payload")
                .getJSONObject(getEntityKey(eventType))
                .getJSONObject("entity");

        switch (eventType) {
            case "payment.captured":
                handlePaymentCaptured(entity);
                break;
            case "payment.failed":
                handlePaymentFailed(entity);
                break;
            case "order.paid":
                handleOrderPaid(entity);
                break;
            default:
                // Unknown event type
                break;
        }
    }

    private void handleOrderPaid(JSONObject order) {
        String orderId = order.getString("id");
        String status = order.optString("status","unknown");
        // Fetch the order and update the payment status
        OrderEntity orderEntity = orderRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));
        // If Already Paid, do nothing
        if (PaymentStatus.PAID.equals(orderEntity.getPaymentStatus())) {
            logger.info("Webhook for paid order ignored. Current status: {}", orderEntity.getPaymentStatus());
            return;
        }
        // Update the payment status
        orderEntity.setPaymentStatus(PaymentStatus.PAID);
        orderEntity.setFailureReason(null);
        orderRepository.save(orderEntity);
        logger.info("Order Paid Successfully. Order ID: {}, Payment Status: {}", orderEntity.getId(),orderEntity.getPaymentStatus());
    }

    private void handlePaymentFailed(JSONObject payment) {
        String paymentId = payment.getString("id");
        String orderId = payment.optString("order_id",null);
        String reason = payment.optString("error_reason","UNKNOWN");
        String description = payment.optString("error_description","No description provided");
        // Fetch the order and update the payment status
        OrderEntity orderEntity = orderRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));
        // If Already Failed Or Paid, do nothing
        if (PaymentStatus.FAILED.equals(orderEntity.getPaymentStatus()) ||
        PaymentStatus.PAID.equals(orderEntity.getPaymentStatus())) {
            logger.info("Webhook for failed payment ignored. Current status: {}", orderEntity.getPaymentStatus());
            return;
        }
        orderEntity.setRazorpayPaymentId(paymentId);
        orderEntity.setPaymentStatus(PaymentStatus.FAILED);
        orderEntity.setFailureReason(reason+": "+description);
        orderRepository.save(orderEntity);
        logger.info("Payment Failed. Order ID: {}, Payment Status: {}", orderEntity.getId(),orderEntity.getPaymentStatus());
    }

    private void handlePaymentCaptured(JSONObject payment) {
        String paymentId = payment.getString("id");
        String orderId = payment.optString("order_id",null);
        BigDecimal amount = BigDecimal.valueOf(payment.getInt("amount")).divide(BigDecimal.valueOf(100));
        // Fetch the order and update the payment status
        OrderEntity orderEntity = orderRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));
        if(PaymentStatus.PAID.equals(orderEntity.getPaymentStatus())) {
            logger.info("Webhook for already paid payment ignored. Current status: {}", orderEntity.getPaymentStatus());
            return;
        }
        orderEntity.setRazorpayPaymentId(paymentId);
        orderEntity.setPaymentStatus(PaymentStatus.PAID);
        orderEntity.setAmount(amount);
        orderEntity.setFailureReason(null);
        orderRepository.save(orderEntity);
        logger.info("Payment Captured Successfully. Order ID: {}, Payment Status: {}", orderEntity.getId(),orderEntity.getPaymentStatus());
    }

    private String getEntityKey(String eventType) {
        if (eventType.startsWith("payment.")) return "payment";
        if (eventType.startsWith("order.")) return "order";
        return "unknown";
    }
}
