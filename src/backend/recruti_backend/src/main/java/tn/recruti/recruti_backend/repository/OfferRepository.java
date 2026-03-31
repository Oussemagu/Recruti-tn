package tn.recruti.recruti_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Offer;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findAll(Pageable pageable);
    
    // Nouvelle méthode pour récupérer les offres d'un recruteur
    Page<Offer> findByRecruteurId(Long recruteurId, Pageable pageable);
    
    // Vérifier si une offre existe déjà avec le même titre et recruteur
    boolean existsByTitreAndRecruteurId(String titre, Long recruteurId);

    
}