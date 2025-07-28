package com.project.aptflow.service.payment;

import com.project.aptflow.dto.payment.OrderDTO;
import java.util.Map;

public interface OrderService {

    Map<String, Object> createOrder(OrderDTO orderDTO);
}
