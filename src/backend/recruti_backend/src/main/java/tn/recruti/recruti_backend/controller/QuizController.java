package tn.recruti.recruti_backend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.recruti.recruti_backend.dto.QuizCreateDto;
import tn.recruti.recruti_backend.dto.QuizSubmissionDto;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.model.Passage;
import tn.recruti.recruti_backend.model.Quiz;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.CandidatureRepository;
import tn.recruti.recruti_backend.repository.OfferRepository;
import tn.recruti.recruti_backend.repository.PassageRepository;
import tn.recruti.recruti_backend.repository.QuizRepository;
import tn.recruti.recruti_backend.repository.UserRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    OfferRepository offerRepository;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    PassageRepository passageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CandidatureRepository candidatureRepository;

    @PostMapping("/createQuiz")
    public ResponseEntity<?> createQuiz (@RequestBody QuizCreateDto dto){
        try {
            Quiz quiz = new Quiz();
            quiz.setContenu(dto.getContenu());
            quiz.setVraiesReponses(dto.getVraiesReponses());

            Offer offer = offerRepository.findById(dto.getOfferId())
                    .orElseThrow(() -> new RuntimeException("Offre introuvable"));

            quiz.setOffer(offer);
            quiz.setPassage(new ArrayList<>());

            offer.setQuiz(quiz);

            offerRepository.save(offer);

            // Return DTO instead of full entity
            QuizResponseDto response = new QuizResponseDto(
                    offer.getQuiz().getId(),
                    offer.getQuiz().getContenu(),
                    offer.getQuiz().getVraiesReponses()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorDto("Error creating quiz: " + e.getMessage()));
        }
    }

    @GetMapping("/getQuiz/{offreId}")
    public ResponseEntity<?> getQuiz(@PathVariable Long offreId){
        try {
            Offer offer = offerRepository.findById(offreId)
                    .orElseThrow(() -> new RuntimeException("Offre not found"));

            if (offer.getQuiz() != null) {
                Quiz quiz = offer.getQuiz();
                // Return DTO instead of full entity
                QuizResponseDto response = new QuizResponseDto(
                        quiz.getId(),
                        quiz.getContenu(),
                        quiz.getVraiesReponses()
                );
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(404).body(new ErrorDto("No quiz associated with this offer"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorDto("Error fetching quiz: " + e.getMessage()));
        }
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

            // Check if candidate has already taken this quiz
            if (passageRepository.findLatestPassageByCandidatAndQuiz(candidat.getId(), quiz.getId()).isPresent()) {
                return ResponseEntity.status(409).body(new ErrorDto("You have already taken this quiz. Each candidate can only take the quiz once."));
            }

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

    @GetMapping("/quiz/{quizId}/passages")
    public ResponseEntity<?> getQuizPassages(@PathVariable Long quizId) {
        try {
            // Verify quiz exists
            if (!quizRepository.existsById(quizId)) {
                return ResponseEntity.status(404).body(new ErrorDto("Quiz not found"));
            }

            // Get quiz and its offer
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz == null || quiz.getOffer() == null) {
                return ResponseEntity.status(404).body(new ErrorDto("Quiz or associated offer not found"));
            }

            List<Passage> passages = passageRepository.findByQuizIdOrderByScoreDesc(quizId);

            // Convert to DTO to expose only needed info
            List<PassageWithCandidateDto> result = passages.stream()
                    .filter(p -> p.getCandidat() != null) // Filter out null candidates
                    .map(p -> {
                        String cvPath = ""; // Default empty
                        // Try to get CV path from candidature
                        // Note: This assumes there's a method to find candidature by candidate and offer
                        try {
                            // For now, we'll leave CV path empty and fetch it from candidature if available
                            cvPath = ""; // Will be fetched on frontend if needed
                        } catch (Exception e) {
                            // If there's an error, just use empty path
                            cvPath = "";
                        }
                        
                        return new PassageWithCandidateDto(
                                p.getId(),
                                p.getCandidat().getId(),
                                p.getCandidat().getNom() != null ? p.getCandidat().getNom() : "N/A",
                                p.getCandidat().getPrenom() != null ? p.getCandidat().getPrenom() : "N/A",
                                p.getCandidat().getEmail() != null ? p.getCandidat().getEmail() : "N/A",
                                p.getScore(),
                                p.getDatePassage(),
                                cvPath
                        );
                    }).toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorDto("Error loading passages: " + e.getMessage()));
        }
    }

    // DTOs
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PassageWithCandidateDto {
        private Long passageId;
        private Long candidatId;
        private String candidatNom;
        private String candidatPrenom;
        private String candidatEmail;
        private int score;
        private LocalDate datePassage;
        private String cvPath;  // CV file path
    }

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
    public static class QuizResponseDto {
        private Long id;
        private String contenu;
        private String vraiesReponses;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorDto {
        private String message;
    }
}
