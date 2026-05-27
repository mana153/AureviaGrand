package com.lodging.Restarurant.model;

import com.lodging.Restarurant.model.enums.BookingStatus;
import com.lodging.Restarurant.model.Payment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Staff member who confirmed/actioned the booking (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    // ── Booking details ───────────────────────────────────────────────────────

    @NotNull
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // ── Timestamps ────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── One-to-one back-reference to Payment (optional) ───────────────────────

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    // ── Convenience helpers ────────────────────────────────────────────────────

    /** Number of nights between check-in and check-out. */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public boolean isCancellable() {
        // Customer can cancel only PENDING or CONFIRMED bookings
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public boolean isPaid() {
        return payment != null &&
                com.lodging.Restarurant.model.enums.PaymentStatus.PAID.equals(payment.getStatus());
    }
}