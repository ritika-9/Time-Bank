package com.timebank.repository;

import com.timebank.entity.ChatMessage;
import com.timebank.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByReferenceIdAndRoomTypeOrderBySentAtAsc(
            Long referenceId, ChatRoomType roomType);
}