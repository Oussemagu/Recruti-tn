package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Passage;

public interface PassageRepository extends JpaRepository<Passage,Long> {
}
