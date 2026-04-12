package tn.recruti.recruti_backend.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import tn.recruti.recruti_backend.dto.ScheduleInterviewRequest;
import tn.recruti.recruti_backend.dto.ScheduleInterviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

        // Flow injected — one token per recruiter stored in ./tokens
        private final GoogleAuthorizationCodeFlow flow;

        @Value("${google.application-name}")
        private String applicationName;

        // ─────────────────────────────────────────────────────────────────────────
        // PUBLIC API
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Creates a Google Calendar event with an auto-generated Meet link.
         * recruiterId is extracted from the JWT in the controller and passed here
         * so we load the correct recruiter's Google token.
         */
        public ScheduleInterviewResponse scheduleInterview(
                        ScheduleInterviewRequest request,
                        String recruiterId) throws IOException, GeneralSecurityException {

                Calendar calendarClient = buildCalendarClient(recruiterId);

                // request is passed to buildCalendarEvent — nothing omitted
                Event event = buildCalendarEvent(request);

                Event created = calendarClient.events()
                                //
                                .insert("primary", event)
                                .setConferenceDataVersion(1) // ← this triggers Meet link generation
                                .setSendUpdates("all") // ← Google sends email invites automatically
                                .execute();

                String meetLink = extractMeetLink(created);
                String eventId = created.getId();

                log.info("Interview scheduled. Event ID: {} | Meet: {}", eventId, meetLink);

                return ScheduleInterviewResponse.builder()
                                .calendarEventId(eventId)
                                .meetLink(meetLink)
                                .startDateTime(request.getStartDateTime())
                                .endDateTime(request.getStartDateTime().plusMinutes(request.getDurationMinutes()))
                                .invitationSent(true)
                                .build();
        }

        /**
         * Updates start/end time of an existing event.
         * The Meet link stays the same — no need to regenerate it.
         */
        public ScheduleInterviewResponse rescheduleInterview(
                        String calendarEventId,
                        ScheduleInterviewRequest request,
                        String recruiterId) throws IOException, GeneralSecurityException {

                Calendar calendarClient = buildCalendarClient(recruiterId);

                Event existing = calendarClient.events()
                                .get("primary", calendarEventId)
                                .execute();

                ZonedDateTime start = request.getStartDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime end = start.plusMinutes(request.getDurationMinutes());

                existing.setStart(new EventDateTime()
                                .setDateTime(new com.google.api.client.util.DateTime(
                                                start.toInstant().toEpochMilli()))
                                .setTimeZone("UTC"));
                existing.setEnd(new EventDateTime()
                                .setDateTime(new com.google.api.client.util.DateTime(
                                                end.toInstant().toEpochMilli()))
                                .setTimeZone("UTC"));

                Event updated = calendarClient.events()
                                .update("primary", calendarEventId, existing)
                                .setSendUpdates("all")
                                .execute();

                String meetLink = extractMeetLink(updated);

                log.info("Interview rescheduled. Event ID: {} | Meet: {}", calendarEventId, meetLink);

                return ScheduleInterviewResponse.builder()
                                .calendarEventId(calendarEventId)
                                .meetLink(meetLink)
                                .startDateTime(request.getStartDateTime())
                                .endDateTime(request.getStartDateTime().plusMinutes(request.getDurationMinutes()))
                                .invitationSent(true)
                                .build();
        }

        /**
         * Deletes the calendar event.
         * Google automatically sends cancellation emails to all attendees.
         */
        public void cancelInterview(
                        String calendarEventId,
                        String recruiterId) throws IOException, GeneralSecurityException {

                Calendar calendarClient = buildCalendarClient(recruiterId);

                calendarClient.events()
                                .delete("primary", calendarEventId)
                                .setSendUpdates("all")
                                .execute();

                log.info("Interview cancelled. Event ID: {}", calendarEventId);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PRIVATE HELPERS
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Builds the Calendar client for a specific recruiter
         * by loading their saved OAuth2 token from ./tokens.
         */
        private Calendar buildCalendarClient(String recruiterId)
                        throws IOException, GeneralSecurityException {

                NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                Credential credential = flow.loadCredential(recruiterId);

                if (credential == null) {
                        throw new IllegalStateException(
                                        "Recruiter " + recruiterId + " has not connected Google Calendar yet. " +
                                                        "Please call GET /api/google/connect first.");
                }

                return new Calendar.Builder(transport, jsonFactory, credential)
                                .setApplicationName(applicationName)
                                .build();
        }

        /**
         * Builds the Google Calendar Event object from the Angular request.
         * request contains all the data — candidate info, time, duration, title.
         */
        private Event buildCalendarEvent(ScheduleInterviewRequest request) {

                ZonedDateTime start = request.getStartDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime end = start.plusMinutes(request.getDurationMinutes());

                EventDateTime startDt = new EventDateTime()
                                .setDateTime(new com.google.api.client.util.DateTime(
                                                start.toInstant().toEpochMilli()))
                                .setTimeZone("UTC");

                EventDateTime endDt = new EventDateTime()
                                .setDateTime(new com.google.api.client.util.DateTime(
                                                end.toInstant().toEpochMilli()))
                                .setTimeZone("UTC");

                // Both recruiter and candidate receive the calendar invite
                List<EventAttendee> attendees = List.of(
                                new EventAttendee().setEmail(request.getRecruiterEmail()),
                                new EventAttendee().setEmail(request.getCandidateEmail()));

                // This is what triggers Google to generate the Meet link
                ConferenceSolutionKey solutionKey = new ConferenceSolutionKey()
                                .setType("hangoutsMeet");

                CreateConferenceRequest conferenceRequest = new CreateConferenceRequest()
                                .setRequestId(UUID.randomUUID().toString()) // must be unique per request
                                .setConferenceSolutionKey(solutionKey);

                ConferenceData conferenceData = new ConferenceData()
                                .setCreateRequest(conferenceRequest);

                String title = (request.getInterviewTitle() != null && !request.getInterviewTitle().isBlank())
                                ? request.getInterviewTitle()
                                : "Interview – " + request.getCandidateName();

                return new Event()
                                .setSummary(title)
                                .setDescription("Scheduled via Recruti.tn platefrom")
                                .setStart(startDt)
                                .setEnd(endDt)
                                .setAttendees(attendees)
                                .setConferenceData(conferenceData);
        }

        /**
         * Extracts the Meet URL from the event's conference entry points.
         * Falls back to hangoutLink for older API responses.
         */
        private String extractMeetLink(Event event) {
                if (event.getConferenceData() != null
                                && event.getConferenceData().getEntryPoints() != null) {

                        return event.getConferenceData().getEntryPoints().stream()
                                        .filter(ep -> "video".equals(ep.getEntryPointType()))
                                        .map(EntryPoint::getUri)
                                        .findFirst()
                                        .orElse(event.getHangoutLink());
                }
                return event.getHangoutLink();
        }
}
