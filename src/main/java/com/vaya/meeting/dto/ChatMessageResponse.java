
package com.vaya.meeting.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String meetingCode,
        Long senderId,
        String senderName,
        String message,
        LocalDateTime sentAt
) {}

