
package com.vaya.meeting.controller;

import com.vaya.meeting.dto.ChatMessageRequest;
import com.vaya.meeting.dto.ChatMessageResponse;
import com.vaya.meeting.service.ChatService;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Valid @Payload ChatMessageRequest request,
            Principal principal) {

        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        ChatMessageResponse response = chatService.sendMessage(
                request.meetingCode(),
                principal.getName(),
                request.message()
        );

        messagingTemplate.convertAndSend(
                "/topic/meetings/" + response.meetingCode(),
                response
        );
    }
}
