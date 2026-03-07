package com.project.chat_server.message.controller;

import com.project.chat_server.message.dto.MessageResponse;
import com.project.chat_server.message.dto.MessageSendRequest;
import com.project.chat_server.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms/{chatRoomId}/messages")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody MessageSendRequest request) {
        return ResponseEntity.ok(MessageResponse.from(
                messageService.sendMessage(chatRoomId, request.senderId(), request.content())));
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessages(chatRoomId, pageable));
    }

    @GetMapping("/slice")
    public ResponseEntity<List<MessageResponse>> getMessagesSlice(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.getMessagesSlice(chatRoomId, pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<Integer> getUnreadCount(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(messageService.getUnreadCount(chatRoomId, userId));
    }

    @PatchMapping("/read")
    public ResponseEntity<Integer> markAllAsRead(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(messageService.markAllAsRead(chatRoomId, userId));
    }
}
