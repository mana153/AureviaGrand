package com.lodging.Restarurant.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_usage_logs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiUsageLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partnerName;
    private String endpoint;
    private String method;
    private int    statusCode;
    private LocalDateTime calledAt;
}