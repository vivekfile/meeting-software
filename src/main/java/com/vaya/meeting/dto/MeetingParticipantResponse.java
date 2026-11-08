
package com.vaya.meeting.dto;

public record MeetingParticipantResponse(
        Long userId,
        String name,
        String role,
        boolean currentUser
) {
}
