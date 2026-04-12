package tn.recruti.recruti_backend.controller;

import tn.recruti.recruti_backend.dto.ScheduleInterviewResponse;
import tn.recruti.recruti_backend.security.JwtUtil;
import tn.recruti.recruti_backend.dto.ScheduleInterviewRequest;
import tn.recruti.recruti_backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j // lombok annootation to generate a logger (log.info, log.error, etc.)
@RestController /// REST controller to handle Http requests and return JSON responses
@RequestMapping("/api/interviews") // Base path for all endpoints in this controller
@RequiredArgsConstructor // Lombok annotation to generate a constructor with required args (final fields)
public class InterviewController {
    private final InterviewService interviewService; // Injected service to handle business logic related to interviews
    private final JwtUtil jwtUtil; // Injected utility to handle JWT token parsing and validation
    // ── POST /api/interviews ─────────────────────────────────────────────────

    /**
     * Called when the recruiter clicks "Send Invitation" in Angular.
     *
     * Angular sends: ScheduleInterviewRequest (candidateName, emails, dateTime…)
     * Spring returns: ScheduleInterviewResponse (meetLink, calendarEventId…)
     */
    @PostMapping // Maps HTTP POST requests to this method
    public ResponseEntity<ScheduleInterviewResponse> scheduleInterview(
            @Valid @RequestBody ScheduleInterviewRequest request,
            @RequestHeader("Authorization") String token) throws Exception {
        // The request body is deserialized into a ScheduleInterviewRequest object and
        // validated
        log.info("Scheduling interview for: {}", request.getCandidateName());
        String recruiterId = jwtUtil.extractId(token.replace("Bearer ", "")); // Extract recruiter ID from JWT token
        ScheduleInterviewResponse response = interviewService.scheduleInterview(request, recruiterId);
        return ResponseEntity.ok(response);

    }

    // ── PUT /api/interviews/{eventId} ────────────────────────────────────────
    /**
     * Called when the recruiter picks a different date/time slot.
     * The Meet link is preserved – only the calendar time is updated.
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<ScheduleInterviewResponse> rescheduleInterview(
            @PathVariable String eventId,
            @Valid @RequestBody ScheduleInterviewRequest request,
            @RequestHeader("Authorization") String token) throws IOException, GeneralSecurityException {

        String recruiterId = jwtUtil.extractId(token.replace("Bearer ", "")); // Extract recruiter ID from JWT token
        log.info("Rescheduling interview. Event ID: {}", eventId);
        ScheduleInterviewResponse response = interviewService.rescheduleInterview(eventId, request, recruiterId);
        return ResponseEntity.ok(response);
    }

    // ── DELETE /api/interviews/{eventId} ─────────────────────────────────────
    /**
     * Called when the recruiter clicks "Cancel".
     * Google sends cancellation emails to all attendees automatically.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> cancelInterview(
            @PathVariable String eventId,
            @RequestHeader("Authorization") String token) throws IOException, GeneralSecurityException {
        String recruiterId = jwtUtil.extractId(token.replace("Bearer ", "")); // Extract recruiter ID from JWT token

        log.info("Cancelling interview. Event ID: {}", eventId);
        interviewService.cancelInterview(eventId, recruiterId);
        return ResponseEntity.noContent().build();
    }

}
