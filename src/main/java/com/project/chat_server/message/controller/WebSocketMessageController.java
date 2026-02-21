package com.project.chat_server.message.controller;

import com.project.chat_server.message.dto.MessageResponse;
import com.project.chat_server.message.dto.MessageSendRequest;
import com.project.chat_server.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;

    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public MessageResponse sendMessage(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request
    ) {
        return MessageResponse.from(messageService.sendMessage(chatRoomId, request.senderId(), request.content()));
    }

}
