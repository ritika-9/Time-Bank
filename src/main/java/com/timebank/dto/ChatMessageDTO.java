package com.timebank.dto;

import com.timebank.entity.ChatRoomType;
import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long referenceId;
    private ChatRoomType roomType;
    private String content;
}