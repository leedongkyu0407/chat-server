package com.project.chat_server.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageSendRequest(
        @NotNull(message = "senderId는 필수입니다.")
        Long senderId,

        @NotBlank(message = "content는 필수입니다.")
        String content
) {
}
