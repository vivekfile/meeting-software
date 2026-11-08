package com.vaya.meeting.service;

import com.vaya.meeting.dto.CreateMeetingRequest;
import com.vaya.meeting.dto.MeetingResponse;
import com.vaya.meeting.entities.Meeting;
import com.vaya.meeting.entities.User;
import com.vaya.meeting.repositories.MeetingRepository;
import com.vaya.meeting.repositories.UserRepository;

import com.vaya.meeting.dto.MeetingParticipantResponse;
import org.springframework.security.access.AccessDeniedException;




import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vaya.meeting.entities.MeetingParticipant;
import com.vaya.meeting.repositories.MeetingParticipantRepository;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository participantRepository;


    public MeetingService(
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            MeetingParticipantRepository participantRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }


    @Transactional(readOnly = true)
    public List<MeetingParticipantResponse> getParticipants(
            String meetingCode,
            String authenticatedEmail
    ) {
        User currentUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new AccessDeniedException("User not found"));

        Meeting meeting = meetingRepository.findByMeetingCode(
                        meetingCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() ->
                        new IllegalArgumentException("Meeting not found"));

        MeetingParticipant currentParticipant =
                participantRepository.findByMeetingIdAndUserId(
                        meeting.getId(),
                        currentUser.getId()
                ).orElseThrow(() ->
                        new AccessDeniedException(
                                "Join the meeting to view its participants"
                        ));

        if (currentParticipant.getLeftAt() != null) {
            throw new AccessDeniedException(
                    "You are no longer an active meeting participant"
            );
        }

        return participantRepository.findByMeetingId(meeting.getId())
                .stream()
                .filter(participant -> participant.getLeftAt() == null)
                .map(participant -> new MeetingParticipantResponse(
                        participant.getUser().getId(),
                        participant.getUser().getName(),
                        participant.getRole(),
                        participant.getUser().getId()
                                .equals(currentUser.getId())
                ))
                .toList();
    }

    @Transactional
    public String joinMeeting(
            String meetingCode,
            String authenticatedEmail
    ) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User account not found"
                        )
                );

        Meeting meeting = meetingRepository.findByMeetingCode(
                        meetingCode.toUpperCase(java.util.Locale.ROOT)
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meeting not found"
                        )
                );

        var existing = participantRepository
                .findByMeetingIdAndUserId(meeting.getId(), user.getId());

        if (existing.isPresent()) {
            MeetingParticipant participant = existing.get();

            if (participant.getLeftAt() == null) {
                return "You are already in this meeting";
            }

            // Rejoin using the existing row because the database
            // has a unique constraint on meeting_id + user_id.
            participant.setJoinedAt(java.time.LocalDateTime.now());
            participant.setLeftAt(null);
            participantRepository.save(participant);

            return "You rejoined the meeting successfully";
        }

        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setRole("PARTICIPANT");
        participant.setJoinedAt(java.time.LocalDateTime.now());
        participant.setLeftAt(null);

        participantRepository.save(participant);

        return "You joined the meeting successfully";
    }


    @Transactional
    public String leaveMeeting(
            String meetingCode,
            String authenticatedEmail
    ) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User account not found"
                        )
                );

        Meeting meeting = meetingRepository.findByMeetingCode(
                        meetingCode.toUpperCase(java.util.Locale.ROOT)
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meeting not found"
                        )
                );

        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meeting.getId(), user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "You have not joined this meeting"
                        )
                );

        if (participant.getLeftAt() != null) {
            return "You have already left this meeting";
        }

        participant.setLeftAt(java.time.LocalDateTime.now());
        participantRepository.save(participant);

        return "You left the meeting successfully";
    }


    @Transactional(readOnly = true)
    public List<String> getParticipants(String meetingCode) {

        Meeting meeting = meetingRepository.findByMeetingCode(
                        meetingCode.toUpperCase(java.util.Locale.ROOT)
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Meeting not found"
                        )
                );

        return participantRepository.findByMeetingId(meeting.getId())
                .stream()
                .filter(participant -> participant.getLeftAt() == null)
                .map(participant -> participant.getUser().getName())
                .toList();
    }

    @Transactional
    public MeetingResponse createMeeting(
            CreateMeetingRequest request,
            String authenticatedEmail
    ) {

        // Find the authenticated user in MySQL.
        User host = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User account not found"
                        )
                );

        // Generate a unique meeting code.
        String meetingCode;

        do {
            meetingCode = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase(Locale.ROOT);

        } while (meetingRepository.existsByMeetingCode(meetingCode));

        // Create the meeting entity.
        Meeting meeting = new Meeting();

        meeting.setMeetingCode(meetingCode);
        meeting.setTitle(request.title().trim());
        meeting.setHost(host);
        meeting.setStatus("CREATED");

        LocalDateTime now = LocalDateTime.now();
        meeting.setCreatedAt(now);

        // Save the meeting.
        Meeting savedMeeting = meetingRepository.save(meeting);

        // Return only the required response fields.
        return new MeetingResponse(
                savedMeeting.getId(),
                savedMeeting.getMeetingCode(),
                savedMeeting.getTitle(),
                savedMeeting.getStatus(),
                savedMeeting.getCreatedAt()
        );
    }
}