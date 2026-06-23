package com.timebank.controller;

import com.timebank.dto.ChatMessageDTO;
import com.timebank.dto.ChatMessageResponse;
import com.timebank.entity.ChatMessage;
import com.timebank.entity.ChatRoomType;
import com.timebank.entity.User;
import com.timebank.repository.ChatMessageRepository;
import com.timebank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    // ⭐ INTERVIEW: @MessageMapping handles WebSocket messages
    // When Angular sends to /app/chat, this method receives it
    // Then broadcasts to /topic/chat/{roomType}/{id}
    // All subscribers to that topic receive it instantly
    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        User sender = userService.getCurrentUser();

        ChatMessage message = ChatMessage.builder()
                .referenceId(dto.getReferenceId())
                .roomType(dto.getRoomType())
                .sender(sender)
                .content(dto.getContent())
                .build();

        chatMessageRepository.save(message);

        ChatMessageResponse response = mapToResponse(message);

        // broadcast to all subscribers of this chat room
        messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getRoomType() + "/" + dto.getReferenceId(),
                response
        );
    }

    // REST endpoint to load chat history
    @GetMapping("/api/chat/{roomType}/{referenceId}")
    public List<ChatMessageResponse> getChatHistory(
            @PathVariable ChatRoomType roomType,
            @PathVariable Long referenceId) {
        return chatMessageRepository
                .findByReferenceIdAndRoomTypeOrderBySentAtAsc(referenceId, roomType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .referenceId(message.getReferenceId())
                .roomType(message.getRoomType())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}