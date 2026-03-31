package tn.recruti.recruti_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.dto.OfferRequestDto;
import tn.recruti.recruti_backend.dto.OfferResponseDto;
import tn.recruti.recruti_backend.dto.PagedResponse;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.service.OfferService;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<Offer> addOffer(@Valid @RequestBody OfferRequestDto dto) {
        return ResponseEntity.status(201).body(offerService.addOffer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(
        @PathVariable Long id,
        @Valid @RequestBody OfferRequestDto dto
    ) {
        return ResponseEntity.ok(offerService.updateOffer(id, dto));
    }
 // Controller — adapter le type de retour
@GetMapping
public ResponseEntity<PagedResponse<OfferResponseDto>> getAllOffers(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "6") int limit
) {
    return ResponseEntity.ok(offerService.getAllOffers(page, limit));
}

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }


    // Ajouter cette méthode après getAllOffers()

/**
 * Récupère toutes les offres d'un recruteur spécifique
 * GET /api/offers/recruiter/{recruiterId}?page=1&limit=6
 */
@GetMapping("/recruiter/{recruiterId}")
public ResponseEntity<PagedResponse<OfferResponseDto>> getOffersByRecruiterId(
    @PathVariable Long recruiterId,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "6") int limit
) {
    return ResponseEntity.ok(offerService.getOffersByRecruiterId(recruiterId, page, limit));
}
}