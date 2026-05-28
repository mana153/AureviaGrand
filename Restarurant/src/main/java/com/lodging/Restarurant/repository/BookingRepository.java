package com.lodging.Restarurant.repository;

import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    java.util.Optional<Booking> findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, BookingStatus status);

    List<Booking> findAllByOrderByCreatedAtDesc();

    List<Booking> findBySourcePartnerOrderByCreatedAtDesc(String sourcePartner);

    @Query("""
        SELECT COUNT(b) = 0 FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT')
        AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)
    """)
    boolean isRoomAvailable(@Param("roomId") Long roomId,
                            @Param("checkIn") LocalDate checkIn,
                            @Param("checkOut") LocalDate checkOut);

    @Query("""
        SELECT COUNT(b) = 0 FROM Booking b
        WHERE b.room.id = :roomId
        AND b.id <> :bookingId
        AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT')
        AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)
    """)
    boolean isRoomAvailableExcluding(@Param("roomId") Long roomId,
                                     @Param("bookingId") Long bookingId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    java.math.BigDecimal totalRevenue();
}