package tn.recruti.recruti_backend.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizCreateDto {
    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;        // JSON des questions

    @NotBlank(message = "Les réponses sont obligatoires")
    private String vraiesReponses; // JSON des réponses

    private Long offerId;          // pour lier à l'offre

}
