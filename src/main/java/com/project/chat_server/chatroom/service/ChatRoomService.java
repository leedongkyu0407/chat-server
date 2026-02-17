package com.project.chat_server.chatroom.service;

import com.project.chat_server.chatroom.domain.ChatRoom;
import com.project.chat_server.chatroom.repository.ChatRoomRepository;
import com.project.chat_server.common.error.code.ErrorCode;
import com.project.chat_server.common.error.exception.BusinessException;
import com.project.chat_server.user.domain.User;
import com.project.chat_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoom createOrGetChatRoom(Long userId1, Long userId2) {
        return chatRoomRepository.findByTwoUsers(userId1, userId2)
                .orElseGet(() -> createChatRoom(userId1, userId2));
    }

    private ChatRoom createChatRoom(Long userId1,  Long userId2) {
        User user1 =  userRepository.findById(userId1)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return chatRoomRepository.save(ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .build());
    }

    @Transactional(readOnly = true)
    public ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRoomsByUserId(Long userId) {
        return chatRoomRepository.findByUserId(userId);
    }
}
