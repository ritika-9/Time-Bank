package com.timebank.dto;

import com.timebank.entity.RequestStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpRequestResponse {
    private Long id;
    private String title;
    private String description;
    private String skillName;
    private String skillCategory;
    private Double hoursRequired;
    private RequestStatus status;
    private String createdByName;
    private Double createdByRating;
    private String acceptedByName;
    private Integer applicantCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}