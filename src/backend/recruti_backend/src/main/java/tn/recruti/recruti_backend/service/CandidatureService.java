package tn.recruti.recruti_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import tn.recruti.recruti_backend.Exception.CandidatureDejaExisteException;
import tn.recruti.recruti_backend.Exception.RessourceNotFoundException;
import tn.recruti.recruti_backend.dto.CandidatureResponseDTO;
import tn.recruti.recruti_backend.dto.CandidatureUpdateDTO;
import tn.recruti.recruti_backend.enums.statuAnalyse;
import tn.recruti.recruti_backend.model.Candidature;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.CandidatureRepository;
import tn.recruti.recruti_backend.repository.OfferRepository;
import tn.recruti.recruti_backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ============ MAPPER ============
    private CandidatureResponseDTO toDTO(Candidature c) {
    CandidatureResponseDTO dto = new CandidatureResponseDTO();
    dto.setIdCandidature(c.getId());
    dto.setIdOffre(c.getOffre() != null ? c.getOffre().getId() : null);
    dto.setDatePostulation(c.getDatePostulation());
    dto.setCvPath(c.getCvPath());
    dto.setScoreCv(c.getScoreCv());
    dto.setStatus(c.getStatus());
    
    // Ajouter les informations du candidat
    if (c.getCandidat() != null) {
        dto.setCandidatNom(c.getCandidat().getNom());
        dto.setCandidatPrenom(c.getCandidat().getPrenom());
        dto.setCandidatEmail(c.getCandidat().getEmail());
    }
    
    return dto;
}

    // ============ POST ============
    public String postuler(Long candidatId, Long offreId, MultipartFile cvFile) throws IOException {

        if (candidatureRepository.existsByCandidat_IdAndOffre_Id(candidatId, offreId)) {
            throw new CandidatureDejaExisteException("Candidature déjà effectuée pour cette offre.");
        }

        User candidat = userRepository.findById(candidatId)
                .orElseThrow(() -> new RessourceNotFoundException("Candidat introuvable avec l'id : " + candidatId));

        Offer offre = offerRepository.findById(offreId)
                .orElseThrow(() -> new RessourceNotFoundException("Offre introuvable avec l'id : " + offreId));

        String fileName = "cv_" + candidatId + "_" + System.currentTimeMillis() + ".pdf";
        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, cvFile.getBytes());

        Candidature candidature = new Candidature();
        candidature.setDatePostulation(LocalDate.now());
        candidature.setCvPath(fileName);
        candidature.setScoreCv(0);
        candidature.setStatus(statuAnalyse.INITIAL);
        candidature.setCandidat(candidat);
        candidature.setOffre(offre);

        candidatureRepository.save(candidature);
        return "Candidature ajoutée avec succès.";
    }

    // ============ GET ============
    public List<CandidatureResponseDTO> getAll() {
        return candidatureRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CandidatureResponseDTO getById(Long id) {
        return toDTO(candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature introuvable avec l'id : " + id)));
    }

    public List<CandidatureResponseDTO> getByCandidat(Long candidatId) {
        return candidatureRepository.findByCandidat_Id(candidatId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CandidatureResponseDTO> getByOffre(Long offreId) {
        return candidatureRepository.findByOffre_Id(offreId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ============ PATCH ============
    public CandidatureResponseDTO update(Long id, CandidatureUpdateDTO dto) throws IOException {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature introuvable avec l'id : " + id));

        if (dto.getCv() != null && !dto.getCv().isEmpty()) {
            String fileName = "cv_" + c.getCandidat().getId() + "_" + System.currentTimeMillis() + ".pdf";
            Path filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, dto.getCv().getBytes());
            c.setCvPath(fileName);
        }

        if (dto.getScoreCv() != null) {
            c.setScoreCv(dto.getScoreCv());
        }

        if (dto.getStatus() != null) {
            c.setStatus(dto.getStatus());
        }

        return toDTO(candidatureRepository.save(c));
    }

    // ============ DELETE ============
    public String delete(Long id) {
        if (!candidatureRepository.existsById(id)) {
            throw new RessourceNotFoundException("Aucune candidature trouvée avec l'id : " + id);
        }
        candidatureRepository.deleteById(id);
        return "Candidature supprimée avec succès.";
    }

    public String updateStatus(Long id, statuAnalyse newStatus) {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Candidature introuvable avec l'id : " + id));
        c.setStatus(newStatus);
        candidatureRepository.save(c);
        return "Statut modifié avec succès.";
    }
}