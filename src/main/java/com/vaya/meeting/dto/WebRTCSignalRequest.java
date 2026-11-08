
package com.vaya.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WebRTCSignalRequest(

        @NotBlank
        @Size(max = 20)
        String meetingCode,

        @NotNull
        Long targetUserId,

        @NotBlank
        String type,

        @Size(max = 1000000)
        String sdp,

        @Size(max = 100000)
        String candidate,

        @Size(max = 100)
        String sdpMid,

        Integer sdpMLineIndex

) {
}
