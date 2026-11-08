
package com.vaya.meeting.controller;

import com.vaya.meeting.dto.CreateMeetingRequest;
import com.vaya.meeting.dto.MeetingResponse;
import com.vaya.meeting.service.MeetingService;

import jakarta.validation.Valid;

import com.vaya.meeting.dto.MeetingParticipantResponse;
import org.springframework.security.access.AccessDeniedException;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    // Create a meeting
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        MeetingResponse response =
                meetingService.createMeeting(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Join a meeting
    @PostMapping("/{meetingCode}/join")
    public ResponseEntity<String> joinMeeting(
            @PathVariable String meetingCode,
            Authentication authentication
    ) {
        String result = meetingService.joinMeeting(
                meetingCode,
                authentication.getName()
        );

        return ResponseEntity.ok(result);
    }

    // Leave a meeting
    @PostMapping("/{meetingCode}/leave")
    public ResponseEntity<String> leaveMeeting(
            @PathVariable String meetingCode,
            Authentication authentication
    ) {
        String result = meetingService.leaveMeeting(
                meetingCode,
                authentication.getName()
        );

        return ResponseEntity.ok(result);
    }

    // Get active participants

    @GetMapping("/{meetingCode}/participants")
    public List<MeetingParticipantResponse> getParticipants(
            @PathVariable String meetingCode,
            Principal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }

        return meetingService.getParticipants(
                meetingCode,
                principal.getName()
        );
    }

}
