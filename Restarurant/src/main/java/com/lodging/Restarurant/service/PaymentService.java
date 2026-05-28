package com.lodging.Restarurant.service;

import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.Payment;
import com.lodging.Restarurant.model.enums.BookingStatus;
import com.lodging.Restarurant.model.enums.PaymentMethod;
import com.lodging.Restarurant.model.enums.PaymentStatus;
import com.lodging.Restarurant.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    public boolean isConfigured() {
        return razorpayKeyId != null && !razorpayKeyId.isBlank()
                && razorpayKeySecret != null && !razorpayKeySecret.isBlank();
    }

    @Transactional
    public Map<String, Object> createOrder(Long bookingId, Long customerId) throws RazorpayException {
        if (!isConfigured()) {
            throw new RuntimeException("Razorpay is not configured. Set razorpay.key.id and razorpay.key.secret.");
        }

        Booking booking = bookingService.getById(bookingId);
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Payment is only available for confirmed bookings.");
        }
        if (booking.isPaid()) {
            throw new RuntimeException("This booking is already paid.");
        }

        int amountPaise = booking.getTotalPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .intValueExact();

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountPaise);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", "booking_" + bookingId);

        com.razorpay.Order order = client.orders.create(orderRequest);
        String orderId = order.get("id");

        Payment payment = paymentRepository.findByBookingId(bookingId).orElseGet(() ->
                Payment.builder()
                        .booking(booking)
                        .amount(booking.getTotalPrice())
                        .method(PaymentMethod.RAZORPAY)
                        .status(PaymentStatus.PENDING)
                        .build());
        payment.setRazorpayOrderId(orderId);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return Map.of(
                "keyId", razorpayKeyId,
                "orderId", orderId,
                "amount", amountPaise,
                "currency", currency,
                "bookingId", bookingId,
                "customerName", booking.getCustomer().getFullName(),
                "customerEmail", booking.getCustomer().getEmail()
        );
    }

    @Transactional
    public void verifyAndCapture(Long bookingId, Long customerId,
                                 String razorpayOrderId,
                                 String razorpayPaymentId,
                                 String razorpaySignature) {
        if (!isConfigured()) {
            throw new RuntimeException("Razorpay is not configured.");
        }

        Booking booking = bookingService.getById(bookingId);
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized.");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            boolean valid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            if (!valid) {
                throw new RuntimeException("Payment verification failed.");
            }
        } catch (Exception e) {
            log.warn("Razorpay verify failed for booking {}", bookingId, e);
            throw new RuntimeException("Payment verification failed.");
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment record not found."));
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}
