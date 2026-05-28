package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.config.ApiPartnerContext;
import com.lodging.Restarurant.controller.api.ApiBookingSupport;
import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.service.BookingService;
import com.lodging.Restarurant.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingApiController {

    private final BookingService bookingService;
    private final RoomService roomService;

    @GetMapping("/rooms")
    public ResponseEntity<?> listRooms(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        if ((checkIn == null) != (checkOut == null)) {
            return ApiBookingSupport.badRequest("Provide both checkIn and checkOut, or neither.");
        }

        List<Room> rooms = (checkIn != null)
                ? roomService.findAll().stream()
                .filter(r -> bookingService.isRoomAvailable(r.getId(), checkIn, checkOut))
                .collect(Collectors.toList())
                : roomService.findAll().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());

        List<Map<String, Object>> result = rooms.stream().map(r -> Map.<String, Object>of(
                "id",            r.getId(),
                "roomNumber",    r.getRoomNumber(),
                "type",          r.getType().name(),
                "pricePerNight", r.getPricePerNight(),
                "capacity",      r.getCapacity(),
                "description",   r.getDescription() != null ? r.getDescription() : "",
                "imageUrl",      r.getImageUrl() != null ? r.getImageUrl() : ""
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        String partner = ApiPartnerContext.getPartnerName(request);
        try {
            Long customerId = ApiBookingSupport.resolveCustomerId(request, body);
            Long roomId     = ApiBookingSupport.requireLong(body, "roomId");
            LocalDate checkIn  = ApiBookingSupport.requireDate(body, "checkIn");
            LocalDate checkOut = ApiBookingSupport.requireDate(body, "checkOut");

            Booking booking = bookingService.createBooking(
                    customerId, roomId, checkIn, checkOut, partner);

            return ResponseEntity.ok(Map.of(
                    "bookingId",  booking.getId(),
                    "status",     booking.getStatus().name(),
                    "totalPrice", booking.getTotalPrice(),
                    "message",    "Booking created successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ApiBookingSupport.badRequest(e.getMessage());
        }
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id, HttpServletRequest request) {
        String partner = ApiPartnerContext.getPartnerName(request);
        try {
            bookingService.assertPartnerOwnsBooking(id, partner);
            Booking b = bookingService.getById(id);
            return ResponseEntity.ok(ApiBookingSupport.toDetail(b));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/bookings/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        String partner = ApiPartnerContext.getPartnerName(request);
        try {
            bookingService.assertPartnerOwnsBooking(id, partner);
            Long customerId = ApiBookingSupport.resolveCustomerId(request, body);
            Long roomId     = ApiBookingSupport.requireLong(body, "roomId");
            LocalDate checkIn  = ApiBookingSupport.requireDate(body, "checkIn");
            LocalDate checkOut = ApiBookingSupport.requireDate(body, "checkOut");

            Booking booking = bookingService.updateBooking(
                    id, customerId, partner, roomId, checkIn, checkOut);

            return ResponseEntity.ok(Map.of(
                    "bookingId",  booking.getId(),
                    "status",     booking.getStatus().name(),
                    "totalPrice", booking.getTotalPrice(),
                    "message",    "Booking updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ApiBookingSupport.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return ApiBookingSupport.badRequest(e.getMessage());
        }
    }

    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        String partner = ApiPartnerContext.getPartnerName(request);
        try {
            bookingService.assertPartnerOwnsBooking(id, partner);
            Long customerId = ApiBookingSupport.resolveCustomerId(request, body);
            bookingService.cancelBooking(id, customerId);
            return ResponseEntity.ok(Map.of(
                    "bookingId", id,
                    "status",    "CANCELLED",
                    "message",   "Booking cancelled successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ApiBookingSupport.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return ApiBookingSupport.badRequest(e.getMessage());
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> listBookings(HttpServletRequest request) {
        String partner = ApiPartnerContext.getPartnerName(request);
        List<Map<String, Object>> result = bookingService.findBySourcePartner(partner).stream()
                .map(ApiBookingSupport::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
