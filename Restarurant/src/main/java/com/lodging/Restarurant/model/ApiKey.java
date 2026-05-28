package com.lodging.Restarurant.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiKey {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyValue;          // e.g. "lh_zomato_abc123"

    private String partnerName;       // "Zomato", "Swiggy", etc.

    /** Customer account this key may book on behalf of (required for secure API use). */
    @Column(name = "customer_id")
    private Long customerId;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}