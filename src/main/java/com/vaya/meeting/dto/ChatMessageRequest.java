
package com.vaya.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank
        @Size(max = 5000)
        String meetingCode,

        @NotBlank
        @Size(max = 2000)
        String message
) {}
