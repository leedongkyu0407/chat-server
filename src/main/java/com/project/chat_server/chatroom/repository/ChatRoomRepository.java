package com.project.chat_server.chatroom.repository;

import com.project.chat_server.chatroom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            SELECT cr FROM ChatRoom cr WHERE 
            (cr.user1.id = :userId1 AND cr.user2.id = :userId2) OR
            (cr.user1.id = :userId2 AND cr.user2.id = :userId1)
            """)
    Optional<ChatRoom> findByTwoUsers(@Param("userId1") Long userId1,
                                      @Param("userId2") Long userId2);

    @Query("""
            SELECT cr FROM ChatRoom cr WHERE cr.user1.id = :userId OR cr.user2.id = :userId
            ORDER BY cr.createdAt DESC
            """)
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
}
