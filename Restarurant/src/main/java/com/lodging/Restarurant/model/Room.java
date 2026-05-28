package com.lodging.Restarurant.model;

import com.lodging.Restarurant.model.enums.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "room_number", nullable = false, unique = true, length = 10)
    private String roomNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;  // SINGLE | DOUBLE | SUITE | DELUXE

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Min(1)
    @Column(nullable = false)
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;  // relative path like /uploads/rooms/room101.jpg

    @Column(name = "is_available")
    @Builder.Default
    private boolean available = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Convenience helper ────────────────────────────────────────────────────

    public String getTypeDisplay() {
        if (type == null) return "";
        return switch (type) {
            case SINGLE -> "Single Room";
            case DOUBLE -> "Double Room";
            case SUITE  -> "Suite";
            case DELUXE -> "Deluxe Room";
        };
    }
}