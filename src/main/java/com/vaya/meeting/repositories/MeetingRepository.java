package com.vaya.meeting.repositories;

import com.vaya.meeting.entities.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRepository
        extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByMeetingCode(String meetingCode);

    boolean existsByMeetingCode(String meetingCode);
}