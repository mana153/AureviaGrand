package com.lodging.Restarurant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank
    @Email
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Please enter a valid email address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Size(min = 6)
    @Column(nullable = false, length = 255)
    private String password;  // BCrypt hash stored here

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Convenience helpers ──────────────────────────────────────────────────

    public String getRoleName() {
        return role != null ? role.getName() : null;
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(getRoleName());
    }

    public boolean isStaff() {
        return "ROLE_STAFF".equals(getRoleName());
    }

    public boolean isCustomer() {
        return "ROLE_CUSTOMER".equals(getRoleName());
    }
}