
package com.vaya.meeting.service;

import com.vaya.meeting.dto.ChatMessageResponse;
import com.vaya.meeting.entities.Meeting;
import com.vaya.meeting.entities.MeetingParticipant;
import com.vaya.meeting.entities.Message;
import com.vaya.meeting.entities.User;
import com.vaya.meeting.repositories.MeetingParticipantRepository;
import com.vaya.meeting.repositories.MeetingRepository;
import com.vaya.meeting.repositories.MessageRepository;
import com.vaya.meeting.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ChatService(
            MessageRepository messageRepository,
            MeetingRepository meetingRepository,
            MeetingParticipantRepository participantRepository,
            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessageResponse sendMessage(
            String meetingCode,
            String authenticatedEmail,
            String text) {

        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        String normalizedCode =
                meetingCode.trim().toUpperCase(Locale.ROOT);

        Meeting meeting = meetingRepository
                .findByMeetingCode(normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meeting not found"));

        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meeting.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Join the meeting before sending messages"));

        if (participant.getLeftAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You have left this meeting");
        }

        String trimmedMessage = text == null ? "" : text.trim();

        if (trimmedMessage.isEmpty() || trimmedMessage.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message must contain between 1 and 2000 characters");
        }

        Message message = new Message();
        message.setMeeting(meeting);
        message.setSender(user);
        message.setMessage(trimmedMessage);
        message.setSentAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);

        return toResponse(saved, normalizedCode, user);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(
            String meetingCode,
            String authenticatedEmail) {

        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        String normalizedCode =
                meetingCode.trim().toUpperCase(Locale.ROOT);

        Meeting meeting = meetingRepository
                .findByMeetingCode(normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meeting not found"));

        participantRepository
                .findByMeetingIdAndUserId(meeting.getId(), user.getId())
                .filter(p -> p.getLeftAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You must be an active participant"));

        return messageRepository
                .findByMeetingIdOrderBySentAtAsc(meeting.getId())
                .stream()
                .map(m -> toResponse(
                        m, normalizedCode, m.getSender()))
                .toList();
    }

    private ChatMessageResponse toResponse(
            Message message,
            String meetingCode,
            User sender) {

        return new ChatMessageResponse(
                message.getId(),
                meetingCode,
                sender.getId(),
                sender.getName(),
                message.getMessage(),
                message.getSentAt()
        );
    }
}
