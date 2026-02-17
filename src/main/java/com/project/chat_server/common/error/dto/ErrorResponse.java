package com.project.chat_server.common.error.dto;

import com.project.chat_server.common.error.code.ErrorCode;

public record ErrorResponse(
        String code,
        String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }
}