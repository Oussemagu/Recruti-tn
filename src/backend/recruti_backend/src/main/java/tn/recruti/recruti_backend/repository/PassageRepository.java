package tn.recruti.recruti_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.recruti.recruti_backend.model.Passage;

public interface PassageRepository extends JpaRepository<Passage,Long> {
    @Query("SELECT p FROM Passage p LEFT JOIN FETCH p.candidat WHERE p.candidat.id = :candidatId AND p.quiz.id = :quizId ORDER BY p.datePassage DESC LIMIT 1")
    Optional<Passage> findLatestPassageByCandidatAndQuiz(@Param("candidatId") Long candidatId, @Param("quizId") Long quizId);
    
    @Query("SELECT p FROM Passage p LEFT JOIN FETCH p.candidat WHERE p.quiz.id = :quizId ORDER BY p.score DESC")
    List<Passage> findByQuizIdOrderByScoreDesc(@Param("quizId") Long quizId);
}
