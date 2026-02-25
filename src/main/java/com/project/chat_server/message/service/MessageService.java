package com.project.chat_server.message.service;

import com.project.chat_server.chatroom.domain.ChatRoom;
import com.project.chat_server.chatroom.repository.ChatRoomRepository;
import com.project.chat_server.common.error.code.ErrorCode;
import com.project.chat_server.common.error.exception.BusinessException;
import com.project.chat_server.message.domain.Message;
import com.project.chat_server.message.repository.MessageRepository;
import com.project.chat_server.user.domain.User;
import com.project.chat_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

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

        return  messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public Page<Message> getMessages(Long chatRoomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Message> getMessagesSlice(Long chatRoomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderByCreatedAtDescSlice(chatRoomId, pageable);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long chatRoomId, Long userId) {
        return messageRepository.countUnreadMessages(chatRoomId, userId);
    }

    @Transactional
    public int markAllAsRead(Long chatRoomId, Long userId) {
        return messageRepository.markAllAsRead(chatRoomId, userId);
    }
}
