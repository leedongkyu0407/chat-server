package com.project.chat_server.message.dto;

import com.project.chat_server.message.domain.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String senderUsername,
        String content,
        boolean read,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}
