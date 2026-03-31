package tn.recruti.recruti_backend.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.dto.CandidatureRequestDTO;
import tn.recruti.recruti_backend.dto.CandidatureResponseDTO;
import tn.recruti.recruti_backend.dto.CandidatureUpdateDTO;
import tn.recruti.recruti_backend.enums.statuAnalyse;
import tn.recruti.recruti_backend.service.CandidatureService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // POST - @Valid vérifie que candidatId, offreId, cv sont présents
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> postuler(
            @ModelAttribute CandidatureRequestDTO dto
    ) throws IOException {
        return ResponseEntity.ok(
                candidatureService.postuler(dto.getCandidatId(), dto.getOffreId(), dto.getCv())
        );
    }

    @GetMapping
    public ResponseEntity<List<CandidatureResponseDTO>> getAll() {
        return ResponseEntity.ok(candidatureService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidatureResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.getById(id));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<CandidatureResponseDTO>> getByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(candidatureService.getByCandidat(candidatId));
    }

    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<CandidatureResponseDTO>> getByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.getByOffre(offreId));
    }

    // PATCH - @ModelAttribute lie les champs form-data au DTO, tout est optionnel
    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<CandidatureResponseDTO> update(
            @PathVariable Long id,
            @ModelAttribute CandidatureUpdateDTO dto
    ) throws IOException {
        return ResponseEntity.ok(candidatureService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam statuAnalyse status
    ) {
        return ResponseEntity.ok(candidatureService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.delete(id));
    }

    // Endpoint pour servir les CVs
    @GetMapping("/cv/{filename:.+}")
    public ResponseEntity<Resource> getCv(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
               return ResponseEntity.ok()
    .contentType(MediaType.APPLICATION_PDF)
    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
    .header("X-Frame-Options", "ALLOWALL")
    .header("Content-Security-Policy", "frame-ancestors *")
    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}