package com.project.chat_server.common.controller;

import com.project.chat_server.chatroom.domain.ChatRoom;
import com.project.chat_server.chatroom.repository.ChatRoomRepository;
import com.project.chat_server.message.domain.Message;
import com.project.chat_server.message.repository.MessageRepository;
import com.project.chat_server.user.domain.User;
import com.project.chat_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test-data")
public class TestDataController {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    @PostMapping("/users")
    public ResponseEntity<String> generateUsers(@RequestParam(defaultValue = "100000") int count) {
        log.debug("사용자 {}명 생성 시작", count);

        int batchSize = Math.min(count, 5000);
        for (int batch = 0; batch < Math.max(1, count / batchSize); batch++) {
            List<User> users = new ArrayList<>();
            for (int i = 1; i <= batchSize; i++) {
                int userId = batch * batchSize + i;
                users.add(User.builder()
                        .username("user" + userId)
                        .password("password" + userId)
                        .build());
            }
            userRepository.saveAll(users);

            if ((batch + 1) % 4 == 0) {  // 2만 명마다 로그
                log.debug("사용자 생성 진행: {}/{}", (batch + 1) * batchSize, count);
            }
        }

        log.debug("사용자 {}명 생성 완료", count);
        return ResponseEntity.ok("사용자 " + count + "명 생성 완료");
    }

    @PostMapping("/chatrooms")
    public ResponseEntity<String> generateChatRooms(@RequestParam(defaultValue = "500") int count) {
        log.debug("채팅방 {}개 생성 시작", count);

        List<User> users = userRepository.findAll();
        if (users.size() < 2) {
            return ResponseEntity.badRequest().body("사용자가 2명 이상 필요합니다");
        }

        List<ChatRoom> chatRooms = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int user1Idx = random.nextInt(users.size());
            int user2Idx = random.nextInt(users.size());
            while (user1Idx == user2Idx) {
                user2Idx = random.nextInt(users.size());
            }

            ChatRoom chatRoom = ChatRoom.builder()
                    .user1(users.get(user1Idx))
                    .user2(users.get(user2Idx))
                    .build();
            chatRooms.add(chatRoom);
        }
        chatRoomRepository.saveAll(chatRooms);

        log.debug("채팅방 {}개 생성 완료", count);
        return ResponseEntity.ok("채팅방 " + count + "개 생성 완료");
    }

    @PostMapping("/messages")
    public ResponseEntity<String> generateMessages(@RequestParam(defaultValue = "100") int messagesPerRoom) {
        log.debug("채팅방당 메시지 {}개 생성 시작", messagesPerRoom);

        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        if (chatRooms.isEmpty()) {
            return ResponseEntity.badRequest().body("채팅방이 필요합니다");
        }

        Random random = new Random();
        int totalMessages = 0;

        for (ChatRoom chatRoom : chatRooms) {
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < messagesPerRoom; i++) {
                User sender = random.nextBoolean() ? chatRoom.getUser1() : chatRoom.getUser2();
                Message message = Message.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .content("테스트 메시지 " + i)
                        .build();
                messages.add(message);
            }
            messageRepository.saveAll(messages);
            totalMessages += messages.size();
        }

        log.debug("총 메시지 {}개 생성 완료", totalMessages);
        return ResponseEntity.ok("총 메시지 " + totalMessages + "개 생성 완료");
    }

    @PostMapping("/all")
    public ResponseEntity<String> generateAll(
            @RequestParam(defaultValue = "100000") int userCount,      // 1000 → 100000
            @RequestParam(defaultValue = "50000") int chatRoomCount,   // 500 → 50000
            @RequestParam(defaultValue = "100") int messagesPerRoom) { // 100 유지

        log.debug("=== 테스트 데이터 생성 시작 ===");
        long startTime = System.currentTimeMillis();

        try {
            generateUsers(userCount);
            generateChatRooms(chatRoomCount);
            generateMessages(messagesPerRoom);

            long duration = (System.currentTimeMillis() - startTime) / 1000;
            int totalMessages = chatRoomCount * messagesPerRoom;

            String result = String.format(
                    "테스트 데이터 생성 완료 (소요 시간: %d초)\n" +
                            "- 사용자: %,d명\n" +
                            "- 채팅방: %,d개\n" +
                            "- 메시지: %,d개",
                    duration, userCount, chatRoomCount, totalMessages
            );

            log.debug(result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("테스트 데이터 생성 실패", e);
            return ResponseEntity.internalServerError()
                    .body("생성 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll() {
        log.debug("모든 테스트 데이터 삭제 시작");

        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        log.debug("모든 테스트 데이터 삭제 완료");
        return ResponseEntity.ok("모든 테스트 데이터 삭제 완료");
    }
}
