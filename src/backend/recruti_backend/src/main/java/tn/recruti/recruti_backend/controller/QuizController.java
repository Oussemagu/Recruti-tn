package tn.recruti.recruti_backend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
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
import tn.recruti.recruti_backend.service.QuizAiService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QuizController {
    @Autowired
    private QuizAiService aiService;

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
    public ResponseEntity<?> createQuiz (@Valid @RequestBody QuizCreateDto dto){
        try {
            if (dto == null || dto.getOfferId() == null) {
                return ResponseEntity.badRequest().body(new ErrorDto("offerId is required"));
            }

            int methode = dto.getMethode();
            if (methode != 0 && methode != 1) {
                return ResponseEntity.badRequest().body(new ErrorDto("methode must be 0 (manual) or 1 (ai)"));
            }

            Offer offer = offerRepository.findById(dto.getOfferId())
                    .orElseThrow(() -> new NoSuchElementException("Offre introuvable"));

            Quiz quiz = new Quiz();
            if (methode == 0) {
                if (isBlank(dto.getContenu()) || isBlank(dto.getVraiesReponses())) {
                    return ResponseEntity.badRequest().body(new ErrorDto("contenu and vraiesReponses are required for manual mode"));
                }

                quiz.setContenu(dto.getContenu());
                quiz.setVraiesReponses(dto.getVraiesReponses());
            } else {
                int requestedCount = dto.getNombreQuestions() == null ? 5 : dto.getNombreQuestions();

                // contenu  "[{'question':'...','choix':['A','B','C']}]"
                // vraiesReponses "['A','C','B']"
                Map<String, String> generated = aiService.generateQuizFromOffre(
                    offer.getDescription(), requestedCount
                );

                String contenu = generated.get("contenu");           // → va dans quiz.setContenu()
                String vraiesReponses = generated.get("vraiesReponses"); // → va dans quiz.setVraiesReponses()
                if (isBlank(vraiesReponses)) {
                    vraiesReponses = buildFallbackAnswersJson(contenu);
                }

                if (isBlank(contenu)
                        || isJsonArrayEmpty(contenu) || isJsonArrayEmpty(vraiesReponses)) {
                    throw new IllegalStateException("AI did not return valid quiz content");
                }

                // Align AI answers with real choice texts so frontend select can prefill correctly.
                vraiesReponses = alignAnswersWithChoices(contenu, vraiesReponses);

                quiz.setContenu(contenu);
                quiz.setVraiesReponses(vraiesReponses);
            }

            quiz.setOffer(offer);
            quiz.setPassage(new ArrayList<>());

            offer.setQuiz(quiz);
            offerRepository.save(offer);

            QuizResponseDto response = new QuizResponseDto(
                    offer.getQuiz().getId(),
                    offer.getQuiz().getContenu(),
                    offer.getQuiz().getVraiesReponses()
            );
            return ResponseEntity.ok(response);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new ErrorDto(
                    "OFFER_NOT_FOUND",
                    e.getMessage(),
                    "Verifie la valeur de offerId et teste GET /api/offers"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorDto(
                    "INVALID_REQUEST",
                    e.getMessage(),
                    "Verifie le body JSON: offerId + methode (0 ou 1)"
            ));
        } catch (IllegalStateException e) {
            String message = e.getMessage() == null ? "Erreur AI" : e.getMessage();
            if (message.toLowerCase().contains("clé api") || message.toLowerCase().contains("api key")
                    || message.toLowerCase().contains("unauthorized") || message.toLowerCase().contains("autorisée")) {
                return ResponseEntity.status(502).body(new ErrorDto(
                        "AI_AUTH_ERROR",
                        message,
                        "Verifie API_KEY (.env), redemarre le backend, puis reteste"
                ));
            }

            return ResponseEntity.status(502).body(new ErrorDto(
                    "AI_PROVIDER_ERROR",
                    message,
                    "Le fournisseur AI a repondu avec une erreur. Verifie le model et la dispo OpenRouter"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorDto(
                    "INTERNAL_ERROR",
                    "Error creating quiz: " + e.getMessage(),
                    "Verifie les logs backend pour plus de details"
            ));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isJsonArrayEmpty(String value) {
        try {
            JsonNode node = new ObjectMapper().readTree(value);
            return node.isArray() && node.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private String buildFallbackAnswersJson(String contenuJson) {
        if (isBlank(contenuJson)) {
            return "[\"A\"]";
        }
        try {
            JsonNode contenu = new ObjectMapper().readTree(contenuJson);
            if (!contenu.isArray() || contenu.isEmpty()) {
                return "[\"A\"]";
            }

            List<String> answers = new ArrayList<>();
            for (JsonNode ignored : contenu) {
                answers.add("A");
            }
            return new ObjectMapper().writeValueAsString(answers);
        } catch (Exception e) {
            return "[\"A\"]";
        }
    }

    private String alignAnswersWithChoices(String contenuJson, String answersJson) {
        if (isBlank(contenuJson)) {
            return answersJson;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode contenuNode = mapper.readTree(contenuJson);
            if (!contenuNode.isArray() || contenuNode.isEmpty()) {
                return answersJson;
            }

            JsonNode answersNode;
            if (isBlank(answersJson)) {
                answersNode = mapper.createArrayNode();
            } else {
                answersNode = mapper.readTree(answersJson);
            }

            List<String> aligned = new ArrayList<>();
            for (int i = 0; i < contenuNode.size(); i++) {
                JsonNode questionNode = contenuNode.get(i);
                JsonNode choixNode = questionNode == null ? null : questionNode.get("choix");

                List<String> choices = new ArrayList<>();
                if (choixNode != null && choixNode.isArray()) {
                    for (JsonNode choiceNode : choixNode) {
                        if (choiceNode != null && !choiceNode.isNull()) {
                            String value = choiceNode.asText();
                            if (!isBlank(value)) {
                                choices.add(value.trim());
                            }
                        }
                    }
                }

                String rawAnswer = null;
                if (answersNode != null && answersNode.isArray() && i < answersNode.size() && !answersNode.get(i).isNull()) {
                    rawAnswer = answersNode.get(i).asText();
                }

                String mapped = mapAnswerTokenToChoice(rawAnswer, choices);
                if (mapped == null) {
                    mapped = choices.isEmpty() ? "A" : choices.get(0);
                }
                aligned.add(mapped);
            }

            return mapper.writeValueAsString(aligned);
        } catch (Exception e) {
            return answersJson;
        }
    }

    private String mapAnswerTokenToChoice(String rawAnswer, List<String> choices) {
        if (isBlank(rawAnswer) || choices == null || choices.isEmpty()) {
            return null;
        }

        String trimmed = rawAnswer.trim();

        for (String choice : choices) {
            if (choice.equalsIgnoreCase(trimmed)) {
                return choice;
            }
        }

        Integer index = toChoiceIndex(trimmed);
        if (index != null && index >= 0 && index < choices.size()) {
            return choices.get(index);
        }

        return null;
    }

    private Integer toChoiceIndex(String token) {
        if (isBlank(token)) {
            return null;
        }

        char first = Character.toUpperCase(token.trim().charAt(0));
        if (first >= 'A' && first <= 'D') {
            return first - 'A';
        }
        if (token.length() == 1 && first >= '1' && first <= '4') {
            return first - '1';
        }
        if (token.length() == 1 && first >= '0' && first <= '3') {
            return first - '0';
        }

        return null;
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

            String safeAnswers = dto.getVraiesReponses();
            if (isBlank(safeAnswers)) {
                safeAnswers = buildFallbackAnswersJson(dto.getContenu());
            }
            if (isBlank(safeAnswers)) {
                safeAnswers = quiz.getVraiesReponses();
            }
            quiz.setVraiesReponses(safeAnswers);

            offer.setQuiz(quiz);

            offerRepository.save(offer);

            return ResponseEntity.ok(quiz);

        }

        return ResponseEntity.notFound().build();

    }


    @DeleteMapping("/deleteQuiz/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId){
        try {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            Offer offer = quiz.getOffer();
            if (offer != null) {
                offer.setQuiz(null);
                offerRepository.save(offer);
            }

            List<Passage> passages = passageRepository.findByQuizIdOrderByScoreDesc(quizId);
            if (passages != null && !passages.isEmpty()) {
                passageRepository.deleteAll(passages);
            }

            quizRepository.delete(quiz);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorDto("Error deleting quiz: " + e.getMessage()));
        }
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
    public static class ErrorDto {
        private String code;
        private String message;
        private String hint;

        public ErrorDto(String message) {
            this.code = "GENERIC_ERROR";
            this.message = message;
            this.hint = null;
        }

        public ErrorDto(String code, String message, String hint) {
            this.code = code;
            this.message = message;
            this.hint = hint;
        }
    }
}
