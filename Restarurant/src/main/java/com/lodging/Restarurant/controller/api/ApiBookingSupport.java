package com.lodging.Restarurant.controller.api;

import com.lodging.Restarurant.config.ApiPartnerContext;
import com.lodging.Restarurant.model.Booking;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

public final class ApiBookingSupport {

    private ApiBookingSupport() {}

    public static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    public static ResponseEntity<Map<String, String>> forbidden(String message) {
        return ResponseEntity.status(403).body(Map.of("error", message));
    }

    /** Uses customer linked to API key; body customerId must match if both are sent. */
    public static Long resolveCustomerId(HttpServletRequest request, Map<String, Object> body) {
        Long keyCustomerId = ApiPartnerContext.getCustomerId(request);
        if (keyCustomerId != null) {
            Object raw = body.get("customerId");
            if (raw != null && !Long.valueOf(raw.toString()).equals(keyCustomerId)) {
                throw new IllegalArgumentException("customerId does not match this API key.");
            }
            return keyCustomerId;
        }
        return requireLong(body, "customerId");
    }

    public static Long requireLong(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return Long.valueOf(raw.toString());
    }

    public static LocalDate requireDate(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return LocalDate.parse(raw.toString());
    }

    public static boolean partnerOwns(Booking booking, String partnerName) {
        return booking.getSourcePartner() != null
                && booking.getSourcePartner().equals(partnerName);
    }

    public static Map<String, Object> toSummary(Booking b) {
        return Map.of(
                "id",         b.getId(),
                "status",     b.getStatus().name(),
                "roomNumber", b.getRoom().getRoomNumber(),
                "checkIn",    b.getCheckInDate().toString(),
                "checkOut",   b.getCheckOutDate().toString(),
                "totalPrice", b.getTotalPrice()
        );
    }

    public static Map<String, Object> toDetail(Booking b) {
        return Map.of(
                "id",          b.getId(),
                "status",      b.getStatus().name(),
                "roomNumber",  b.getRoom().getRoomNumber(),
                "checkIn",     b.getCheckInDate().toString(),
                "checkOut",    b.getCheckOutDate().toString(),
                "totalPrice",  b.getTotalPrice(),
                "guest",       b.getCustomer().getFullName()
        );
    }
}
