package com.project.chat_server.grpc;

import com.project.chat_server.message.dto.MessageResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ChatGrpcService extends ChatServiceGrpc.ChatServiceImplBase {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendMessage(ChatMessageRequest request,
                            StreamObserver<ChatMessageResponse> responseObserver) {
        try {
            MessageResponse messageResponse = new MessageResponse(
                    request.getChatRoomId(),
                    request.getChatRoomId(),
                    request.getSenderId(),
                    request.getSenderUsername(),
                    request.getContent(),
                    false,
                    LocalDateTime.parse(request.getCreatedAt())
            );

            messagingTemplate.convertAndSend(
                    "/topic/chat/" + request.getChatRoomId(),
                    messageResponse);

            responseObserver.onNext(ChatMessageResponse.newBuilder()
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC 메시지 전달 실패: {}", e.getMessage());
            responseObserver.onNext(ChatMessageResponse.newBuilder()
                    .setSuccess(false)
                    .build());
            responseObserver.onCompleted();
        }
    }
}