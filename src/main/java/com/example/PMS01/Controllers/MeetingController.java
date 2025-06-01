
package com.example.PMS01.controllers;

import com.example.PMS01.dto.MeetingDTO;
import com.example.PMS01.services.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<MeetingDTO> createMeeting(@RequestBody MeetingDTO meetingDTO) {
        MeetingDTO createdMeeting = meetingService.createMeeting(meetingDTO);
        return new ResponseEntity<>(createdMeeting, HttpStatus.CREATED);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MeetingDTO>> getProjectMeetings(@PathVariable Long projectId) {
        List<MeetingDTO> meetings = meetingService.getProjectMeetings(projectId);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/my-meetings")
    public ResponseEntity<List<MeetingDTO>> getUserMeetings() {
        List<MeetingDTO> meetings = meetingService.getUserMeetings();
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MeetingDTO>> getUpcomingMeetings() {
        List<MeetingDTO> meetings = meetingService.getUpcomingMeetings();
        return ResponseEntity.ok(meetings);
    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingDTO> updateMeeting(
            @PathVariable Long meetingId,
            @RequestBody MeetingDTO meetingDTO) {
        MeetingDTO updatedMeeting = meetingService.updateMeeting(meetingId, meetingDTO);
        return ResponseEntity.ok(updatedMeeting);
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.noContent().build();
    }
}