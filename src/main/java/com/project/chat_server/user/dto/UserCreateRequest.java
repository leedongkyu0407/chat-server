package com.project.chat_server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "username은 필수입니다.")
        @Size(min = 2, max = 50, message = "username은 2자 이상 50자 이하입니다.")
        String username,

        @NotBlank(message = "password는 필수입니다.")
        String password
) {
}
