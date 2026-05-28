package com.lodging.Restarurant.model;

import com.lodging.Restarurant.model.enums.RoomOrderBillingType;
import com.lodging.Restarurant.model.enums.RoomOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(name = "walk_in_name", length = 100)
    private String walkInName;

    @Column(name = "venue_slug", nullable = false, length = 100)
    private String venueSlug;

    @Column(name = "venue_name", nullable = false, length = 150)
    private String venueName;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type", nullable = false, length = 30)
    private RoomOrderBillingType billingType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private RoomOrderStatus status = RoomOrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "roomOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomOrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
