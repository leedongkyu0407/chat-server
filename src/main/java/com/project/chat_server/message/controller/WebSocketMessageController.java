package com.project.chat_server.message.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chat_server.grpc.ChatGrpcClient;
import com.project.chat_server.message.dto.MessageResponse;
import com.project.chat_server.message.dto.MessageSendRequest;
import com.project.chat_server.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatGrpcClient chatGrpcClient;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/grpc/{chatRoomId}")
    public void sendMessageGrpc(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request) {

        MessageResponse messageResponse = MessageResponse.from(
                messageService.sendMessage(chatRoomId, request.senderId(), request.content()));

        chatGrpcClient.sendMessage(
                chatRoomId,
                messageResponse.senderId(),
                messageResponse.senderUsername(),
                messageResponse.content(),
                messageResponse.createdAt().toString()
        );
    }

    @MessageMapping("/chat/redis/{chatRoomId}")
    public void sendMessageRedis(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request) throws JsonProcessingException {

        MessageResponse messageResponse = MessageResponse.from(
                messageService.sendMessage(chatRoomId, request.senderId(), request.content()));

        // Redis에 메시지 발행만
        String channel = "chat:" + chatRoomId;
        redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(messageResponse));
    }

    @MessageMapping("/chat/grpc/test/{chatRoomId}")
    public void sendMessageGrpcTest(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request) {

        MessageResponse messageResponse = new MessageResponse(
                0L, chatRoomId, request.senderId(),
                "test", request.content(), false,
                LocalDateTime.now());

        chatGrpcClient.sendMessage(
                chatRoomId,
                messageResponse.senderId(),
                messageResponse.senderUsername(),
                messageResponse.content(),
                messageResponse.createdAt().toString()
        );
    }

    @MessageMapping("/chat/redis/test/{chatRoomId}")
    public void sendMessageRedisTest(
            @DestinationVariable Long chatRoomId,
            MessageSendRequest request) throws JsonProcessingException {

        MessageResponse messageResponse = new MessageResponse(
                0L, chatRoomId, request.senderId(),
                "test", request.content(), false,
                LocalDateTime.now());

        // Redis에 메시지 발행만
        String channel = "chat:" + chatRoomId;
        redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(messageResponse));
    }
}
