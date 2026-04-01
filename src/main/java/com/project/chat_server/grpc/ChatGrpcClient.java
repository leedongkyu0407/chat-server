package com.project.chat_server.grpc;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatGrpcClient {

    @GrpcClient("chat-server")
    private ChatServiceGrpc.ChatServiceBlockingStub chatServiceStub;

    public boolean sendMessage(long chatRoomId, long senderId,
                               String senderUsername, String content, String createdAt) {
        try {
            ChatMessageRequest request = ChatMessageRequest.newBuilder()
                    .setChatRoomId(chatRoomId)
                    .setSenderId(senderId)
                    .setSenderUsername(senderUsername)
                    .setContent(content)
                    .setCreatedAt(createdAt)
                    .build();

            ChatMessageResponse response = chatServiceStub.sendMessage(request);
            return response.getSuccess();

        } catch (Exception e) {
            log.error("gRPC 메시지 전송 실패: {}", e.getMessage());
            return false;
        }
    }
}