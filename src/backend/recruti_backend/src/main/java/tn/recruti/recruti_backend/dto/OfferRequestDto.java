package tn.recruti.recruti_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OfferRequestDto(
    @NotNull LocalDate dateEmission,
    @NotBlank String titre,
    @NotBlank String description,
    String tags,
    boolean available,
    @NotNull Long idRecruteur
) {}