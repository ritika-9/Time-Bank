package com.timebank.dto;

import com.timebank.entity.SessionStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private String title;
    private String description;
    private String skillName;
    private String skillCategory;
    private Double credits;
    private Double duration;
    private SessionStatus status;
    private String offeredByName;
    private Double offeredByRating;
    private String bookedByName;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}