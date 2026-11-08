package com.vaya.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMeetingRequest(

        @NotBlank(message = "Meeting title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title

) {
}