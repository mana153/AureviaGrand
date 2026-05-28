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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return createBooking(customerId, roomId, checkIn, checkOut, null);
    }

    @Transactional
    public Booking createBooking(Long customerId, Long roomId,
                                 LocalDate checkIn, LocalDate checkOut,
                                 String sourcePartner) {

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

        if (!room.isAvailable()) {
            throw new RuntimeException("This room is not available for booking.");
        }

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
                .sourcePartner(sourcePartner)
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
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be confirmed.");
        }
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found."));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setAssignedBy(staff);
        bookingRepository.save(booking);
    }

    @Transactional
    public void rejectBooking(Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be rejected.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void checkIn(Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed bookings can be checked in.");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    @Transactional
    public void checkOut(Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Only checked-in bookings can be checked out.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
    }

    public static boolean countsTowardRevenue(BookingStatus status) {
        return status == BookingStatus.CONFIRMED
                || status == BookingStatus.CHECKED_IN
                || status == BookingStatus.CHECKED_OUT;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Booking> findByCustomer(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Booking findActiveCheckedInBooking(Long customerId) {
        return bookingRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(
                        customerId, BookingStatus.CHECKED_IN)
                .orElseThrow(() -> new RuntimeException(
                        "You need an active checked-in stay to charge food orders to your room."));
    }

    public List<Booking> findAll() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Booking> findBySourcePartner(String sourcePartner) {
        return bookingRepository.findBySourcePartnerOrderByCreatedAtDesc(sourcePartner);
    }

    public void assertPartnerOwnsBooking(Long bookingId, String partnerName) {
        Booking booking = getById(bookingId);
        if (booking.getSourcePartner() == null
                || !booking.getSourcePartner().equals(partnerName)) {
            throw new RuntimeException("Booking not found.");
        }
    }

    public Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found."));
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return bookingRepository.isRoomAvailable(roomId, checkIn, checkOut);
    }

    @Transactional
    public Booking updateBooking(Long bookingId, Long customerId, String partnerName,
                                 Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Booking booking = getById(bookingId);

        if (partnerName != null) {
            if (booking.getSourcePartner() == null
                    || !booking.getSourcePartner().equals(partnerName)) {
                throw new RuntimeException("Booking not found.");
            }
        } else if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be updated.");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Check-out must be after check-in.");
        }

        if (!bookingRepository.isRoomAvailableExcluding(roomId, bookingId, checkIn, checkOut)) {
            throw new RuntimeException("Room is not available for selected dates.");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found."));
        if (!room.isAvailable()) {
            throw new RuntimeException("This room is not available for booking.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));

        return bookingRepository.save(booking);
    }

    public Map<String, Object> getAnalyticsSummary() {
        List<Booking> bookings = findAll();

        long pendingCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long confirmedCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long checkedInCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN).count();
        long checkedOutCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_OUT).count();
        long cancelledCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> countsTowardRevenue(b.getStatus()))
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Map<String, Object>> revenueByType = bookings.stream()
                .filter(b -> countsTowardRevenue(b.getStatus()))
                .collect(Collectors.groupingBy(
                        b -> b.getRoom().getType().name(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("bookingCount", (long) list.size());
                            stats.put("revenue", list.stream()
                                    .map(Booking::getTotalPrice)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add));
                            return stats;
                        })
                ));

        List<String> revenueLabels = List.copyOf(revenueByType.keySet());
        List<BigDecimal> revenueValues = revenueLabels.stream()
                .map(label -> (BigDecimal) revenueByType.get(label).get("revenue"))
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("pendingCount", pendingCount);
        summary.put("confirmedCount", confirmedCount);
        summary.put("checkedInCount", checkedInCount);
        summary.put("checkedOutCount", checkedOutCount);
        summary.put("cancelledCount", cancelledCount);
        summary.put("totalRevenue", totalRevenue);
        summary.put("revenueByType", revenueByType);
        summary.put("revenueLabels", revenueLabels);
        summary.put("revenueValues", revenueValues);
        return summary;
    }
}