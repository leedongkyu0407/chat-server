package com.project.chat_server.chatroom.dto;

import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequest(
        @NotNull(message = "상대방 userId는 필수입니다.")
        Long targetUserId
) {
}
