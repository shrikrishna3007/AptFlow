package com.project.aptflow.service.payment.impl;

import com.project.aptflow.dto.payment.OrderDTO;
import com.project.aptflow.entity.payment.OrderEntity;
import com.project.aptflow.enums.PaymentStatus;
import com.project.aptflow.exceptions.OrderCreationException;
import com.project.aptflow.mapper.payment.OrderMapper;
import com.project.aptflow.repository.payment.OrderRepository;
import com.project.aptflow.service.payment.OrderService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Value("${razorpay.key.id}")
    private String razorpayKeyID;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(razorpayKeyID, razorpayKeySecret);
    }

    @Override
    public Map<String, Object> createOrder(OrderDTO orderDTO) {
        try {
            long amountInPaise = orderDTO.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("amount", amountInPaise);
            jsonObject.put("currency", "INR");
            jsonObject.put("receipt", orderDTO.getGenerateBillEntity().getUserEntity().getEmail());

            Order order = razorpayClient.orders.create(jsonObject);

            orderDTO.setRazorpayOrderId(order.get("id"));
            orderDTO.setPaymentStatus(PaymentStatus.CREATED);

            logger.info("Order created successfully. Order ID: {}, Status: {}", orderDTO.getRazorpayOrderId(), orderDTO.getPaymentStatus());

            Map<String, Object> data = new HashMap<>();
            data.put("razorpay_order_id", orderDTO.getRazorpayOrderId());
            data.put("amount", amountInPaise);
            data.put("status", orderDTO.getPaymentStatus());

            OrderEntity orderEntity = orderMapper.dtoToEntity(orderDTO);
            orderRepository.save(orderEntity);

            logger.info("Order saved successfully. Order ID: {}, GenerateBill ID: {}", orderEntity.getId(), orderEntity.getGenerateBillEntity().getId());

            return data;
        }catch (RazorpayException e) {
            logger.error("Failed To Create Razorpay Order: {}", e.getMessage(), e);
            throw new OrderCreationException("Error in creating order", e);
        }
    }
}
