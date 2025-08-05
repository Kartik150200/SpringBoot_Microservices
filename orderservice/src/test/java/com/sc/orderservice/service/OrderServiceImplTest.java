package com.sc.orderservice.service;

import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import com.sc.orderservice.external.client.PaymentService;
import com.sc.orderservice.external.client.ProductService;
import com.sc.orderservice.repository.OrderRepository;

@SpringBootTest
public class OrderServiceImplTest {
    
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    
}
