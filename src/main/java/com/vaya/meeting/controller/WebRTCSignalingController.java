
package com.vaya.meeting.controller;

import com.vaya.meeting.dto.WebRTCSignalRequest;
import com.vaya.meeting.dto.WebRTCSignalResponse;
import com.vaya.meeting.service.WebRTCSignalingService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebRTCSignalingController {

    private final WebRTCSignalingService signalingService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebRTCSignalingController(
            WebRTCSignalingService signalingService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.signalingService = signalingService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/webrtc.signal")
    public void handleSignal(
            @Valid @Payload WebRTCSignalRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        WebRTCSignalResponse response =
                signalingService.prepareSignal(
                        request,
                        principal.getName()
                );

        String targetEmail =
                signalingService.getTargetEmail(
                        request.targetUserId()
                );

        messagingTemplate.convertAndSendToUser(
                targetEmail,
                "/queue/webrtc",
                response
        );
    }
}
