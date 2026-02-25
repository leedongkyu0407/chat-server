package com.project.chat_server.message.repository;

import com.project.chat_server.message.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
//    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    // N+1 해결: fetch join 추가
    @Query("""
            SELECT m FROM Message m
            JOIN FETCH m.sender
            JOIN FETCH m.chatRoom
            WHERE m.chatRoom.id = :chatRoomId
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(
            @Param("chatRoomId") Long chatRoomId,
            Pageable pageable);

    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        JOIN FETCH m.chatRoom
        WHERE m.chatRoom.id = :chatRoomId
        ORDER BY m.createdAt DESC
        """)
    Slice<Message> findByChatRoomIdOrderByCreatedAtDescSlice(
            @Param("chatRoomId") Long chatRoomId,
            Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM Message m 
            WHERE m.chatRoom.id = :chatRoomId
            AND m.sender.id != :userId AND m.read = false
            """)
    int countUnreadMessages(@Param("chatRoomId") Long chatRoomId,
                            @Param("userId") Long userId);

    @Modifying
    @Query("""
            UPDATE Message m SET m.read = true
            WHERE m.chatRoom.id = :chatRoomId
            AND m.sender.id != :userId AND m.read = false
            """)
    int markAllAsRead(@Param("chatRoomId") Long chatRoomId,
                      @Param("userId") Long userId);
}
