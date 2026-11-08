package com.vaya.meeting.dto;

public record UserResponse(
        Long id,
        String name,
        String email
) {}