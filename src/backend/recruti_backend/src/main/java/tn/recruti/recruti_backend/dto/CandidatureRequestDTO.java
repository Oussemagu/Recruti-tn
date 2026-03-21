package tn.recruti.recruti_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CandidatureRequestDTO {

    @NotNull(message = "candidatId est obligatoire")
    private Long candidatId;

    @NotNull(message = "offreId est obligatoire")
    private Long offreId;

    @NotNull(message = "cv est obligatoire")
    private MultipartFile cv;

    // optionnel
    private Integer scoreCv;
}