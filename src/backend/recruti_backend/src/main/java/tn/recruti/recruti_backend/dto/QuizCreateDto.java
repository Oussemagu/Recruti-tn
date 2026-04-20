package tn.recruti.recruti_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizCreateDto {
    private String contenu; // JSON des questions

    private String vraiesReponses; // JSON des réponses

    @NotNull(message = "offerId is required")
    private Long offerId; // pour lier à l'offre

    @NotNull(message = "methode is required")
    @Min(value = 0, message = "methode must be 0 or 1")
    @Max(value = 1, message = "methode must be 0 or 1")
    private Integer methode; // 0: classique , 1 : avec ia

    @Min(value = 1, message = "nombreQuestions must be >= 1")
    @Max(value = 50, message = "nombreQuestions must be <= 50")
    private Integer nombreQuestions;

}
