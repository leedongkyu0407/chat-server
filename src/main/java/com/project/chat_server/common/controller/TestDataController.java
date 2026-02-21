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
    public ResponseEntity<String> generateUsers(@RequestParam(defaultValue = "1000") int count) {
        log.info("사용자 {}명 생성 시작", count);

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .password("password" + i)
                    .build();
            users.add(user);
        }
        userRepository.saveAll(users);

        log.info("사용자 {}명 생성 완료", count);
        return ResponseEntity.ok("사용자 " + count + "명 생성 완료");
    }

    @PostMapping("/chatrooms")
    public ResponseEntity<String> generateChatRooms(@RequestParam(defaultValue = "500") int count) {
        log.info("채팅방 {}개 생성 시작", count);

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

        log.info("채팅방 {}개 생성 완료", count);
        return ResponseEntity.ok("채팅방 " + count + "개 생성 완료");
    }

    @PostMapping("/messages")
    public ResponseEntity<String> generateMessages(@RequestParam(defaultValue = "100") int messagesPerRoom) {
        log.info("채팅방당 메시지 {}개 생성 시작", messagesPerRoom);

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

        log.info("총 메시지 {}개 생성 완료", totalMessages);
        return ResponseEntity.ok("총 메시지 " + totalMessages + "개 생성 완료");
    }

    @PostMapping("/all")
    public ResponseEntity<String> generateAll(
            @RequestParam(defaultValue = "1000") int userCount,
            @RequestParam(defaultValue = "500") int chatRoomCount,
            @RequestParam(defaultValue = "100") int messagesPerRoom) {

        log.info("전체 테스트 데이터 생성 시작");

        generateUsers(userCount);
        generateChatRooms(chatRoomCount);
        generateMessages(messagesPerRoom);

        int totalMessages = chatRoomCount * messagesPerRoom;

        String result = String.format(
                "테스트 데이터 생성 완료\n- 사용자: %d명\n- 채팅방: %d개\n- 메시지: %d개",
                userCount, chatRoomCount, totalMessages
        );

        log.info(result);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll() {
        log.info("모든 테스트 데이터 삭제 시작");

        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        log.info("모든 테스트 데이터 삭제 완료");
        return ResponseEntity.ok("모든 테스트 데이터 삭제 완료");
    }
}
