package com.group2.payment_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.group2.payment_service.dto.PaymentRequest;
import com.group2.payment_service.dto.PaymentResponse;
import com.group2.payment_service.dto.PaymentVerifyRequest;
import com.group2.payment_service.service.PaymentService;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController controller;

    @Test
    void testCreateOrder() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId("order_123");
        when(paymentService.createOrder(any(PaymentRequest.class))).thenReturn(response);

        ResponseEntity<?> res = controller.createOrder(new PaymentRequest());
        assertEquals(200, res.getStatusCode().value());
        PaymentResponse body = (PaymentResponse) res.getBody();
        assertEquals("order_123", body.getOrderId());
    }

    @Test
    void testVerifyPayment_Success() {
        when(paymentService.verifyPayment(any(PaymentVerifyRequest.class))).thenReturn("Payment Verification Successful");

        ResponseEntity<String> res = controller.verifyPayment(new PaymentVerifyRequest());
        assertEquals(200, res.getStatusCode().value());
        assertEquals("Payment Verification Successful", res.getBody());
    }

    @Test
    void testVerifyPayment_Failure() {
        when(paymentService.verifyPayment(any(PaymentVerifyRequest.class))).thenReturn("Invalid Signature");

        ResponseEntity<String> res = controller.verifyPayment(new PaymentVerifyRequest());
        assertEquals(400, res.getStatusCode().value());
        assertEquals("Invalid Signature", res.getBody());
    }
}
