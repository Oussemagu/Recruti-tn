package tn.recruti.recruti_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response returned to Angular after the interview is scheduled.
 *
 * Angular receives:
 * {
 * "calendarEventId": "abc123xyz",
 * "meetLink": "meet.google.com/abc-defg-hij",
 * "startDateTime": "2024-09-29T09:00:00",
 * "endDateTime": "2024-09-29T10:00:00",
 * "invitationSent": true
 * }
 */
@Data
@Builder
public class ScheduleInterviewResponse {

    private String calendarEventId;
    private String meetLink;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean invitationSent;
}
