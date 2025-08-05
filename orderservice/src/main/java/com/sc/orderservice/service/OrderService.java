package com.sc.orderservice.service;

import com.sc.orderservice.model.OrderRequest;
import com.sc.orderservice.model.OrderResponse;

public interface OrderService {

    long placeOrder(OrderRequest orderRequest);

    public OrderResponse getOrderDetails(long orderId);

}
