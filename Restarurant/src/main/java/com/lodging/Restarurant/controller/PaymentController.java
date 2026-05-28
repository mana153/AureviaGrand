package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.service.PaymentService;
import com.lodging.Restarurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customer/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/bookings/{bookingId}/create-order")
    public ResponseEntity<?> createOrder(@PathVariable Long bookingId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Map<String, Object> order = paymentService.createOrder(bookingId, user.getId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/razorpay/create-order/{bookingId}")
    public ResponseEntity<?> createOrderAlias(@PathVariable Long bookingId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return createOrder(bookingId, userDetails);
    }

    @PostMapping("/bookings/{bookingId}/verify")
    public ResponseEntity<?> verify(@PathVariable Long bookingId,
                                    @RequestParam String razorpay_order_id,
                                    @RequestParam String razorpay_payment_id,
                                    @RequestParam String razorpay_signature,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            paymentService.verifyAndCapture(bookingId, user.getId(),
                    razorpay_order_id, razorpay_payment_id, razorpay_signature);
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment successful."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/razorpay/verify-payment/{bookingId}")
    public ResponseEntity<?> verifyAlias(@PathVariable Long bookingId,
                                         @RequestParam String razorpay_order_id,
                                         @RequestParam String razorpay_payment_id,
                                         @RequestParam String razorpay_signature,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        return verify(bookingId, razorpay_order_id, razorpay_payment_id, razorpay_signature, userDetails);
    }
}
