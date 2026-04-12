package tn.recruti.recruti_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Payload received from the Angular "Schedule Interview" form.
 *
 * Angular sends:
 * {
 * "candidateName": "John Doe",
 * "candidateEmail": "john.doe@techgiant.com",
 * "recruiterEmail": "alex.sterling@curator.com",
 * "startDateTime": "2024-09-29T09:00:00",
 * "durationMinutes": 60,
 * "interviewTitle": "Final Executive Round – John Doe"
 * }
 */
@Data
public class ScheduleInterviewRequest {

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @Email(message = "Invalid candidate email")
    @NotBlank(message = "Candidate email is required")
    private String candidateEmail;

    @Email(message = "Invalid recruiter email")
    @NotBlank(message = "Recruiter email is required")
    private String recruiterEmail;

    @NotNull(message = "Start date/time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "Duration is required")
    private Integer durationMinutes;

    private String interviewTitle;
}
