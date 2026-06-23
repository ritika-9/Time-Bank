package com.timebank.dto;

import com.timebank.entity.ChatRoomType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long referenceId;
    private ChatRoomType roomType;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
}