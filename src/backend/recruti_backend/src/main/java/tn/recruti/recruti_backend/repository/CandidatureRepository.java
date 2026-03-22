package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.recruti.recruti_backend.model.Candidature;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findByCandidat_Id(Long candidatId);
    List<Candidature> findByOffre_Id(Long offreId);

     // Pour vérifier le doublon 409
    boolean existsByCandidat_IdAndOffre_Id(Long candidatId, Long offreId);
}