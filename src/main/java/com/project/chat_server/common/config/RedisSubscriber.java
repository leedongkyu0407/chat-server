package com.project.chat_server.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chat_server.message.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("Received a message from channel: {}", new String(message.getBody()));
        try {
            message.getBody();
            String publishMessage = new String(message.getBody());

            MessageResponse messageResponse = objectMapper.readValue(publishMessage, MessageResponse.class);
            messagingTemplate.convertAndSend("/topic/chat/" + messageResponse.chatRoomId(), messageResponse);
        } catch (JsonProcessingException e) {
            log.error("Redis 메시지 역직렬화 실패: {}", e.getMessage());
        }
    }
}
