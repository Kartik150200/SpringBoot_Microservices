package com.sc.paymentservice.service;

import com.sc.paymentservice.model.PaymentRequest;
import com.sc.paymentservice.model.PaymentResponse;

public interface PaymentService {

    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);

}
