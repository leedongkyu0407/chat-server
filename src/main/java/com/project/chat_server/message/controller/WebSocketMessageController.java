package com.project.chat_server.message.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chat_server.message.dto.MessageResponse;
import com.project.chat_server.message.dto.MessageSendRequest;
import com.project.chat_server.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request) throws JsonProcessingException {

        MessageResponse messageResponse = MessageResponse.from(
                messageService.sendMessage(chatRoomId, request.senderId(), request.content()));

        // Redis에 메시지 발행만
        String channel = "chat:" + chatRoomId;
        redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(messageResponse));
    }

    @MessageMapping("/chat/test/{chatRoomId}")
    public void sendMessageTest(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request) throws JsonProcessingException {

        // DB 저장 없이 Redis 발행만
        MessageResponse messageResponse = new MessageResponse(
                0L, chatRoomId, request.senderId(),
                "test", request.content(), false,
                LocalDateTime.now());

        String channel = "chat:" + chatRoomId;
        redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(messageResponse));
    }
}
