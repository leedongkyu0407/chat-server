package com.project.chat_server.chatroom.controller;

import com.project.chat_server.chatroom.dto.ChatRoomCreateRequest;
import com.project.chat_server.chatroom.dto.ChatRoomResponse;
import com.project.chat_server.chatroom.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createOrGetChatRoom(
            @RequestParam Long userId,
            @Valid @RequestBody ChatRoomCreateRequest request) {
        return ResponseEntity.ok(ChatRoomResponse.from(
                chatRoomService.createOrGetChatRoom(userId, request.targetUserId())));
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(ChatRoomResponse.from(
                chatRoomService.getChatRoom(chatRoomId)));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomsByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(userId)
                .stream()
                .map(ChatRoomResponse::from)
                .toList());
    }
}
