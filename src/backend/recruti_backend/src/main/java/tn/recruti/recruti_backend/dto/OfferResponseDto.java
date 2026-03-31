package tn.recruti.recruti_backend.dto;

import java.time.LocalDate;

// OfferResponseDto.java
public record OfferResponseDto(
    Long id,
    LocalDate dateEmission,
    String titre,
    String description,
    String tags,
    boolean available,
    Long recruteurId
) {}