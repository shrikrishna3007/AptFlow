package com.project.aptflow.controller.payment;

import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.payment.OrderDTO;
import com.project.aptflow.service.payment.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/razorpay-orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public ResponseEntity<ResponseDTO<Map<String,Object>>> createOrder(@RequestBody OrderDTO orderDTO) {
        Map<String,Object> data =  orderService.createOrder(orderDTO);
        ResponseDTO<Map<String,Object>> responseDTO = new ResponseDTO<>(
                "Payment order created successfully",  HttpStatus.OK, data
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }
}
