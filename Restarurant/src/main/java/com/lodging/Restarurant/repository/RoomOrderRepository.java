package com.lodging.Restarurant.repository;

import com.lodging.Restarurant.model.RoomOrder;
import com.lodging.Restarurant.model.enums.RoomOrderBillingType;
import com.lodging.Restarurant.model.enums.RoomOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomOrderRepository extends JpaRepository<RoomOrder, Long> {
    List<RoomOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o FROM RoomOrder o WHERE o.booking.id = :bookingId ORDER BY o.createdAt DESC")
    List<RoomOrder> findByBookingIdOrderByCreatedAtDesc(@Param("bookingId") Long bookingId);
    long countByStatus(RoomOrderStatus status);
    List<RoomOrder> findByBillingTypeOrderByCreatedAtDesc(RoomOrderBillingType billingType);
}
