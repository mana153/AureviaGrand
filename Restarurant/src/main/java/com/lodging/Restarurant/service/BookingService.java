package com.lodging.Restarurant.service;

import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.BookingStatus;
import com.lodging.Restarurant.repository.BookingRepository;
import com.lodging.Restarurant.repository.RoomRepository;
import com.lodging.Restarurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public Booking createBooking(Long customerId, Long roomId,
                                 LocalDate checkIn, LocalDate checkOut) {

        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Check-out must be after check-in.");
        }

        boolean available = bookingRepository.isRoomAvailable(roomId, checkIn, checkOut);
        if (!available) {
            throw new RuntimeException("Room is not available for selected dates.");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found."));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found."));

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .customer(customer)
                .room(room)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalPrice(total)
                .status(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Transactional
    public void cancelBooking(Long bookingId, Long customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found."));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized.");
        }

        if (!booking.isCancellable()) {
            throw new RuntimeException("This booking cannot be cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // ── Staff actions ─────────────────────────────────────────────────────────

    @Transactional
    public void confirmBooking(Long bookingId, Long staffId) {
        Booking booking = getById(bookingId);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found."));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setAssignedBy(staff);
        bookingRepository.save(booking);
    }

    @Transactional
    public void rejectBooking(Long bookingId) {
        Booking booking = getById(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void checkIn(Long bookingId) {
        Booking booking = getById(bookingId);
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    @Transactional
    public void checkOut(Long bookingId) {
        Booking booking = getById(bookingId);
        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Booking> findByCustomer(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found."));
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return bookingRepository.isRoomAvailable(roomId, checkIn, checkOut);
    }
}