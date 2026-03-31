package tn.recruti.recruti_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import tn.recruti.recruti_backend.dto.OfferRequestDto;
import tn.recruti.recruti_backend.dto.OfferResponseDto;
import tn.recruti.recruti_backend.dto.PagedResponse;
import tn.recruti.recruti_backend.Exception.DuplicateResourceException;
import tn.recruti.recruti_backend.Exception.RessourceNotFoundException;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.OfferRepository;
import tn.recruti.recruti_backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

   // MODIFIER la méthode addOffer() pour ajouter la validation

public Offer addOffer(OfferRequestDto dto) {
    User recruteur = userRepository.findById(dto.idRecruteur())
        .orElseThrow(() -> new RessourceNotFoundException(
            "Recruteur not found: " + dto.idRecruteur()));

    // Vérifier si une offre avec le même titre existe déjà pour ce recruteur
    if (offerRepository.existsByTitreAndRecruteurId(dto.titre(), dto.idRecruteur())) {
        throw new DuplicateResourceException(
            "Une offre avec le titre '" + dto.titre() + "' existe déjà pour ce recruteur");
    }

    Offer offer = new Offer();
    offer.setDateEmission(dto.dateEmission());
    offer.setTitre(dto.titre());
    offer.setDescription(dto.description());
    offer.setTags(dto.tags());
    offer.setAvailable(dto.available());
    offer.setRecruteur(recruteur);

    return offerRepository.save(offer);
}
    // ── Update ───────────────────────────────────────────
    public Offer updateOffer(Long id, OfferRequestDto dto) {
        Offer offer = offerRepository.findById(id)
            .orElseThrow(() -> new RessourceNotFoundException("Offer not found: " + id));

        User recruteur = userRepository.findById(dto.idRecruteur())
            .orElseThrow(() -> new RessourceNotFoundException(
                "Recruteur not found: " + dto.idRecruteur()));

        offer.setDateEmission(dto.dateEmission());
        offer.setTitre(dto.titre());
        offer.setDescription(dto.description());
        offer.setTags(dto.tags());
        offer.setAvailable(dto.available());
        offer.setRecruteur(recruteur);

        return offerRepository.save(offer);
    }

    // ── Get all (paginé) ──────────────────────────────────

    // Dans OfferService.java
private OfferResponseDto toDto(Offer offer) {
    return new OfferResponseDto(
        offer.getId(),
        offer.getDateEmission(),
        offer.getTitre(),
        offer.getDescription(),
        offer.getTags(),
        offer.isAvailable(),
        offer.getRecruteur() != null ? offer.getRecruteur().getId() : null
    );
}

public PagedResponse<OfferResponseDto> getAllOffers(int page, int limit) {
    Pageable pageable = PageRequest.of(page - 1, limit,
        Sort.by("dateEmission").descending());
    Page<Offer> result = offerRepository.findAll(pageable);

    return new PagedResponse<>(
        result.getContent().stream().map(this::toDto).toList(),
        page,
        limit,
        result.getTotalElements(),
        result.getTotalPages()
    );
}

// Ajouter cette méthode après getAllOffers()

/**
 * Récupère toutes les offres d'un recruteur spécifique (paginé)
 * GET /api/offers/recruiter/{recruiterId}
 */
public PagedResponse<OfferResponseDto> getOffersByRecruiterId(Long recruiterId, int page, int limit) {
    // Vérifier que le recruteur existe
    if (!userRepository.existsById(recruiterId)) {
        throw new RessourceNotFoundException("Recruteur not found: " + recruiterId);
    }
    
    Pageable pageable = PageRequest.of(page - 1, limit,
        Sort.by("dateEmission").descending());
    Page<Offer> result = offerRepository.findByRecruteurId(recruiterId, pageable);

    return new PagedResponse<>(
        result.getContent().stream().map(this::toDto).toList(),
        page,
        limit,
        result.getTotalElements(),
        result.getTotalPages()
    );
}
   // ── Get by id ─────────────────────────────────────────
    public Offer getOfferById(Long id) {
        return offerRepository.findById(id)
            .orElseThrow(() -> new RessourceNotFoundException("Offer not found: " + id));
    }

    // ── Delete ────────────────────────────────────────────
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RessourceNotFoundException("Offer not found: " + id);
        }
        offerRepository.deleteById(id);
    }
}