package com.project.chat_server.message.controller;

import com.project.chat_server.message.dto.MessageResponse;
import com.project.chat_server.message.dto.MessageSendRequest;
import com.project.chat_server.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessages(chatRoomId, pageable)
                .map(MessageResponse::from));
    }

    @GetMapping("/slice")
    public ResponseEntity<Slice<MessageResponse>> getMessagesSlice(
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesSlice(chatRoomId, pageable)
                .map(MessageResponse::from));
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
