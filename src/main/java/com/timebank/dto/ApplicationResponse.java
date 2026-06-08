package com.timebank.dto;

import com.timebank.entity.ApplicationStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long requestId;
    private String requestTitle;
    private Long applicantId;
    private String applicantName;
    private Double applicantRating;
    private String message;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}