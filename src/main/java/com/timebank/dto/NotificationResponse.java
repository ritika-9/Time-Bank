package com.timebank.dto;

import com.timebank.entity.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private Boolean isRead;
    private NotificationType type;
    private Long referenceId;
    private LocalDateTime createdAt;
}