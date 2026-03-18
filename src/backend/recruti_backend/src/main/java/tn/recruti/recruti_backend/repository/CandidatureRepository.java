package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Candidature;

public interface CandidatureRepository extends JpaRepository<Candidature,Long> {
}
