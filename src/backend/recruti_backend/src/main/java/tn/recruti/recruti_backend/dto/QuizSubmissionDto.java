package tn.recruti.recruti_backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class QuizSubmissionDto {
    private Long quizId;           // ID of the quiz
    private Long candidatId;       // ID of the candidate taking the quiz
    private List<String> answers;  // List of answers in order (e.g., ["A", "C", "B"])
}
