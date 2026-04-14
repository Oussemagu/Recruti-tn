package tn.recruti.recruti_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.dto.QuizCreateDto;
import tn.recruti.recruti_backend.dto.QuizSubmissionDto;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.model.Passage;
import tn.recruti.recruti_backend.model.Quiz;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.OfferRepository;
import tn.recruti.recruti_backend.repository.PassageRepository;
import tn.recruti.recruti_backend.repository.QuizRepository;
import tn.recruti.recruti_backend.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class QuizController {

    @Autowired
    OfferRepository offerRepository;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    PassageRepository passageRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/createQuiz")
    public Quiz createQuiz (@RequestBody QuizCreateDto dto){
        Quiz quiz=new Quiz();
        quiz.setContenu(dto.getContenu());
        quiz.setVraiesReponses(dto.getVraiesReponses());


        Offer offer= offerRepository.findById(dto.getOfferId())
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));

        quiz.setOffer(offer);
        quiz.setPassage(new ArrayList<>());

        offer.setQuiz(quiz);

        offerRepository.save(offer);

        return offer.getQuiz();

    }

    @GetMapping("/getQuiz/{offreId}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Long offreId){
        Offer offer= offerRepository.findById(offreId)
                .orElseThrow(()->new RuntimeException("Offre not found"));

        if(offer.getQuiz()!=null){
            return ResponseEntity.ok(offer.getQuiz());
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping("/updateQuiz")
    public ResponseEntity<Quiz> updateQuiz (@RequestBody QuizCreateDto dto){
        Offer offer=offerRepository.findById(dto.getOfferId())
                .orElseThrow(()->new RuntimeException("Offer not found for this Quiz"));

        Quiz quiz=offer.getQuiz();

        if(quiz !=null) {
            quiz.setContenu(dto.getContenu());
            quiz.setVraiesReponses(dto.getVraiesReponses());

            offer.setQuiz(quiz);

            offerRepository.save(offer);

            return ResponseEntity.ok(quiz);

        }

        return ResponseEntity.notFound().build();

    }


    @DeleteMapping("/deleteQuiz/{quizId}")
    public Quiz deleteQuiz(@PathVariable Long quizId){
        Quiz quiz=quizRepository.findById(quizId)
                .orElseThrow(()->new RuntimeException("Quiz not found "));
        quizRepository.deleteById(quizId);
        return quiz;
    }

    @PostMapping("/submitQuiz")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmissionDto dto) {
        try {
            // Get the quiz
            Quiz quiz = quizRepository.findById(dto.getQuizId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            // Get the candidate
            User candidat = userRepository.findById(dto.getCandidatId())
                    .orElseThrow(() -> new RuntimeException("Candidate not found"));

            // Parse the correct answers
            List<String> correctAnswers = new ArrayList<>();
            try {
                String vraiesReponses = quiz.getVraiesReponses();
                if (vraiesReponses != null) {
                    // vraiesReponses is a JSON array string like ["A", "C", "B"]
                    String cleanJson = vraiesReponses.replace("[", "").replace("]", "").replace("\"", "");
                    if (!cleanJson.isEmpty()) {
                        String[] answers = cleanJson.split(",");
                        for (String answer : answers) {
                            correctAnswers.add(answer.trim());
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error parsing correct answers: " + e.getMessage());
            }

            // Calculate score
            int score = 0;
            if (dto.getAnswers() != null && !correctAnswers.isEmpty()) {
                int maxQuestions = Math.min(dto.getAnswers().size(), correctAnswers.size());
                for (int i = 0; i < maxQuestions; i++) {
                    if (dto.getAnswers().get(i) != null && 
                        dto.getAnswers().get(i).equalsIgnoreCase(correctAnswers.get(i))) {
                        score++;
                    }
                }
                // Convert to percentage
                score = (int) ((score * 100) / correctAnswers.size());
            }

            // Create and save passage
            Passage passage = new Passage();
            passage.setDatePassage(LocalDate.now());
            passage.setScore(score);
            passage.setQuiz(quiz);
            passage.setCandidat(candidat);

            passageRepository.save(passage);

            // Return result
            return ResponseEntity.ok(new PassageResultDto(
                    passage.getId(),
                    passage.getScore(),
                    correctAnswers.size(),
                    passage.getDatePassage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorDto(e.getMessage()));
        }
    }

    // Helper DTOs
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PassageResultDto {
        private Long passageId;
        private int score;
        private int totalQuestions;
        private LocalDate datePassage;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorDto {
        private String message;
    }

