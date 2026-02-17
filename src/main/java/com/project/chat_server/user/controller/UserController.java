package com.project.chat_server.user.controller;

import com.project.chat_server.user.dto.UserCreateRequest;
import com.project.chat_server.user.dto.UserResponse;
import com.project.chat_server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.createUser(request.username(), request.password())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(UserResponse.from(userService.getUser(userId)));
    }
}
