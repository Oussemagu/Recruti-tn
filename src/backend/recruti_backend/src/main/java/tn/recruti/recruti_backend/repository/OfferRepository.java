package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Offer;

import java.time.LocalDate;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer,Long> {
    List<Offer> findByTitre(String titre);                        // par titre exact

    List<Offer> findByTitreContaining(String titre);              // titre contient le mot

    List<Offer> findByAvailable(boolean available);               // disponible ou non

    List<Offer> findByDateEmission(LocalDate dateEmission);       // par date exacte

    List<Offer> findByDateEmissionBetween(LocalDate debut, LocalDate fin);  // entre deux dates

    List<Offer> findByAvailableAndTitreContaining(boolean available, String titre); // combiné
}
