package com.project.chat_server.message.repository;

import com.project.chat_server.chatroom.domain.ChatRoom;
import com.project.chat_server.message.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
