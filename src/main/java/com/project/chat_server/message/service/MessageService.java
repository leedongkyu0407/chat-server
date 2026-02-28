package com.project.chat_server.message.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chat_server.chatroom.domain.ChatRoom;
import com.project.chat_server.chatroom.repository.ChatRoomRepository;
import com.project.chat_server.common.error.code.ErrorCode;
import com.project.chat_server.common.error.exception.BusinessException;
import com.project.chat_server.message.domain.Message;
import com.project.chat_server.message.dto.MessageResponse;
import com.project.chat_server.message.repository.MessageRepository;
import com.project.chat_server.user.domain.User;
import com.project.chat_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "messages:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);

    @Transactional
    public Message sendMessage(Long chatRoomId, Long senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .build();

        evictCache(chatRoomId);
        return  messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long chatRoomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable)
                .getContent().stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesSlice(Long chatRoomId, Pageable pageable) {
        String key = CACHE_PREFIX + chatRoomId + ":slice:" + pageable.getPageNumber();

        // 캐시 조회
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            List<MessageResponse> result = deserialize(cached);
            if (result != null) {
                log.debug("cache hit: {}", result);
                return result;
            }

            redisTemplate.delete(key);  // 역직렬화 실패한 캐시 삭제
        }

        List<MessageResponse> result = messageRepository
                .findByChatRoomIdOrderByCreatedAtDescSlice(chatRoomId, pageable)
                .getContent().stream()
                .map(MessageResponse::from)
                .toList();

        String serialized = serialize(result);
        log.info("serialized: {}", serialized);  // ← 추가
        if (serialized != null) {
            redisTemplate.opsForValue().set(key, serialized, CACHE_TTL);
            log.info("Redis 저장 완료: key={}", key);  // ← 추가
        }

        return result;
    }

    private String serialize(Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            log.info("직렬화 성공: {}", json.substring(0, Math.min(100, json.length())));
            return json;        } catch (JsonProcessingException e) {
            log.warn("Redis 직렬화 실패, 캐시 저장 생략: {}", e.getMessage());
            return null;  // 예외 던지지 않고 null 반환
        }
    }

    private List<MessageResponse> deserialize(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Redis 역직렬화 실패, DB 조회로 fallback: {}", e.getMessage());
            return null;  // null 반환하면 DB 조회로 넘어감
        }
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long chatRoomId, Long userId) {
        return messageRepository.countUnreadMessages(chatRoomId, userId);
    }

    @Transactional
    public int markAllAsRead(Long chatRoomId, Long userId) {
        evictCache(chatRoomId);
        return messageRepository.markAllAsRead(chatRoomId, userId);
    }

    private void evictCache(Long chatRoomId) {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + chatRoomId + ":slice:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
