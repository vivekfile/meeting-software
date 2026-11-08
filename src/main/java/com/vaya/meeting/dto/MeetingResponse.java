package com.vaya.meeting.dto;

import java.time.LocalDateTime;

public record MeetingResponse(
        Long id,
        String meetingCode,
        String title,
        String status,
        LocalDateTime createdAt
) {
}