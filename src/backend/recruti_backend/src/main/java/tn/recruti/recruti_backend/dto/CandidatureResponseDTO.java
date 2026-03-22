package tn.recruti.recruti_backend.dto;

import lombok.Data;
import tn.recruti.recruti_backend.enums.statuAnalyse;
import java.time.LocalDate;

@Data
public class CandidatureResponseDTO {
    private Long idCandidature;   // id de la candidature
    private Long idOffre;         // id de l'offre
    private LocalDate datePostulation;
    private String cvPath;
    private int scoreCv;
    private statuAnalyse status;
}