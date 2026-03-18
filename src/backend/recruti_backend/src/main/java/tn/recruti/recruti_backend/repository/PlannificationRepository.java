package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Plannification;

public interface PlannificationRepository extends JpaRepository<Plannification,Long> {
}
