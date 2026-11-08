
package com.vaya.meeting.dto;

public record WebRTCSignalResponse(
        String meetingCode,
        Long senderId,
        String senderName,
        String type,
        String sdp,
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex
) {
}
