package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz,Long> {
}
