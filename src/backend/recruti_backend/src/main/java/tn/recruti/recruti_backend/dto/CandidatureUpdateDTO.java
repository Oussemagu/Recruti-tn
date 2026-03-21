package tn.recruti.recruti_backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import tn.recruti.recruti_backend.enums.statuAnalyse;

@Data
public class CandidatureUpdateDTO {
    // tout optionnel
    private MultipartFile cv;
    private Integer scoreCv;
    private statuAnalyse status;
}