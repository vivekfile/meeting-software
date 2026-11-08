
package com.vaya.meeting.service;

import com.vaya.meeting.dto.WebRTCSignalRequest;
import com.vaya.meeting.dto.WebRTCSignalResponse;
import com.vaya.meeting.entities.Meeting;
import com.vaya.meeting.entities.MeetingParticipant;
import com.vaya.meeting.entities.User;
import com.vaya.meeting.repositories.MeetingParticipantRepository;
import com.vaya.meeting.repositories.MeetingRepository;
import com.vaya.meeting.repositories.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class WebRTCSignalingService {

    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;

    public WebRTCSignalingService(
            UserRepository userRepository,
            MeetingRepository meetingRepository,
            MeetingParticipantRepository participantRepository
    ) {
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional(readOnly = true)
    public WebRTCSignalResponse prepareSignal(
            WebRTCSignalRequest request,
            String authenticatedEmail
    ) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new AccessDeniedException("Authentication required");
        }

        String meetingCode = request.meetingCode()
                .trim()
                .toUpperCase(Locale.ROOT);

        String type = request.type()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!type.equals("OFFER")
                && !type.equals("ANSWER")
                && !type.equals("ICE_CANDIDATE")) {
            throw new IllegalArgumentException(
                    "Unsupported signaling type"
            );
        }

        if (type.equals("OFFER") || type.equals("ANSWER")) {
            if (request.sdp() == null || request.sdp().isBlank()) {
                throw new IllegalArgumentException(
                        "SDP is required for offers and answers"
                );
            }
        }

        if (type.equals("ICE_CANDIDATE")) {
            if (request.candidate() == null
                    || request.candidate().isBlank()) {
                throw new IllegalArgumentException(
                        "ICE candidate is required"
                );
            }
        }

        User sender = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new AccessDeniedException("User not found"));

        if (request.targetUserId().equals(sender.getId())) {
            throw new IllegalArgumentException(
                    "You cannot send a signal to yourself"
            );
        }

        Meeting meeting = meetingRepository
                .findByMeetingCode(meetingCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Meeting not found"));

        User target = userRepository.findById(request.targetUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Target user not found"));

        MeetingParticipant senderParticipant =
                participantRepository.findByMeetingIdAndUserId(
                        meeting.getId(), sender.getId()
                ).orElseThrow(() ->
                        new AccessDeniedException(
                                "Join the meeting before calling"
                        ));

        MeetingParticipant targetParticipant =
                participantRepository.findByMeetingIdAndUserId(
                        meeting.getId(), target.getId()
                ).orElseThrow(() ->
                        new AccessDeniedException(
                                "Target user is not a participant"
                        ));

        if (senderParticipant.getLeftAt() != null) {
            throw new AccessDeniedException(
                    "You are no longer an active meeting participant"
            );
        }

        if (targetParticipant.getLeftAt() != null) {
            throw new AccessDeniedException(
                    "Target user is not currently in the meeting"
            );
        }

        return new WebRTCSignalResponse(
                meetingCode,
                sender.getId(),
                sender.getName(),
                type,
                request.sdp(),
                request.candidate(),
                request.sdpMid(),
                request.sdpMLineIndex()
        );
    }

    @Transactional(readOnly = true)
    public String getTargetEmail(Long targetUserId) {
        return userRepository.findById(targetUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Target user not found"))
                .getEmail();
    }
}
