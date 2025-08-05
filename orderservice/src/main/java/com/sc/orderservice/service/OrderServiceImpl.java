package com.sc.orderservice.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sc.orderservice.entity.Order;
import com.sc.orderservice.exception.CustomException;
import com.sc.orderservice.external.client.PaymentService;
import com.sc.orderservice.external.client.ProductService;
import com.sc.orderservice.external.request.PaymentRequest;
import com.sc.orderservice.external.response.PaymentResponse;
import com.sc.orderservice.model.OrderRequest;
import com.sc.orderservice.model.OrderResponse;
import com.sc.orderservice.repository.OrderRepository;
import com.sc.productservice.model.ProductResponse;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order Entity -> Save the data with Status Order Created.
        //Product Service - Book Products (Reduce the Quantity)
        //Paymeny Service -> payments -> Success -> COMPLETE, Else
        //CANCELLED

        log.info("Placing Order Request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                            .amount(orderRequest.getTotalAmount())
                            .orderStatus("CREATED")
                            .productId(orderRequest.getProductId())
                            .orderDate(Instant.now())
                            .quantity(orderRequest.getQuantity())
                            .build();
        order = orderRepository.save(order);

        log.info("Calling PAyment Service to complete the payment");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                                                        .orderId(order.getId())
                                                        .paymentMode(orderRequest.getPaymentMode())
                                                        .amount(orderRequest.getTotalAmount())
                                                        .build();
        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully.Changing the Order Status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occured in payment. Changing Order Status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order placed successfully with Id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get Order Details for Order Id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                                    .orElseThrow(() -> new CustomException("Order not found for the order Id: " + orderId,
                                    "NOT_FOUND",
                                    404));
        
        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());
        ProductResponse productResponse = restTemplate.getForObject(
            "http://productservice/product/" + order.getProductId(),
            ProductResponse.class
        );

        log.info("Getting Payment Information from the payment Service");
        PaymentResponse paymentResponse = restTemplate.getForObject(
            "http://paymentservice/payment/order/" + order.getId(),
            PaymentResponse.class
        );

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails
                                                                    .builder()
                                                                    .productName(productResponse.getProductName())
                                                                    .productId(productResponse.getProductId())
                                                                    .build();

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails
                                                                    .builder()
                                                                    .paymentId(paymentResponse.getPaymentId())
                                                                    .paymentStatus(paymentResponse.getStatus())
                                                                    .paymentDate(paymentResponse.getPaymentDate())
                                                                    .paymentMode(paymentResponse.getPaymentMode())
                                                                    .build();


        OrderResponse orderResponse = OrderResponse.builder()
                                                    .orderId(order.getId())
                                                    .orderStatus(order.getOrderStatus())
                                                    .amount(order.getAmount())
                                                    .orderDate(order.getOrderDate())
                                                    .productDetails(productDetails)
                                                    .paymentDetails(paymentDetails)
                                                    .build();

        
       return orderResponse;
    }

}
