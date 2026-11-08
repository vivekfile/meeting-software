package com.vaya.meeting.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}