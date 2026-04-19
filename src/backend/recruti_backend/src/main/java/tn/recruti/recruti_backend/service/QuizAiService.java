package tn.recruti.recruti_backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizAiService {

    @Value("${api.key:}")
    private String apiKey;

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openrouter/auto";

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> generateQuizFromOffre(String offreDescription, int nombreQuestions) throws Exception {
        if (offreDescription == null || offreDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("La description de l'offre est requise pour générer le quiz");
        }
        String normalizedApiKey = normalizeApiKey(apiKey);
        if (normalizedApiKey == null || normalizedApiKey.isEmpty()) {
            throw new IllegalStateException("La clé API est manquante");
        }

        String prompt = """
                Tu es un expert RH. À partir de cette offre d'emploi, génère exactement %d questions QCM.
                
                Offre: %s
                
                Réponds UNIQUEMENT avec ce JSON (sans texte autour) :
                {
                  "contenu": [
                    {"question": "...", "choix": ["A. ...", "B. ...", "C. ...", "D. ..."]}
                  ],
                  "vraiesReponses": ["A", "C", ...]
                }
                """.formatted(nombreQuestions, offreDescription);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + normalizedApiKey);
        headers.set("HTTP-Referer", "http://localhost");
        headers.set("X-Title", "Recruti");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(API_URL, request, Map.class);
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new IllegalStateException("Clé API invalide ou non autorisée");
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("Erreur du fournisseur AI: HTTP " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString());
        }

        if (response.getBody() == null || response.getBody().get("choices") == null) {
            throw new IllegalStateException("Le fournisseur AI a retourné une réponse vide");
        }

        List<Map> choices = (List<Map>) response.getBody().get("choices");
        if (choices.isEmpty() || choices.get(0).get("message") == null) {
            throw new IllegalStateException("Format de réponse AI invalide");
        }

        Map message = (Map) choices.get(0).get("message");
        String content = (String) message.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("Le fournisseur AI a retourné un contenu vide");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(extractJsonPayload(content));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de parser la réponse AI: " + content);
        }

        JsonNode contenuNode = root.get("contenu");
        if (contenuNode == null || !contenuNode.isArray() || contenuNode.isEmpty()) {
            throw new IllegalStateException("La réponse AI ne contient pas un champ contenu valide");
        }

        ArrayNode answersNode = extractAnswers(root, contenuNode, mapper);
        if (answersNode.isEmpty() || answersNode.size() != contenuNode.size()) {
            answersNode = recoverAnswersWithSecondPass(contenuNode, mapper, normalizedApiKey);
        }

        if (answersNode.isEmpty()) {
            throw new IllegalStateException("La réponse AI ne contient pas de vraies réponses");
        }

        answersNode = normalizeAnswersSize(answersNode, contenuNode.size(), mapper);

        return Map.of(
                "contenu", contenuNode.toString(),
                "vraiesReponses", answersNode.toString()
        );
    }

    private ArrayNode extractAnswers(JsonNode root, JsonNode contenuNode, ObjectMapper mapper) {
        JsonNode directAnswers = firstPresent(root,
                "vraiesReponses",
                "vraies_reponses",
                "correctAnswers",
                "correct_answers");

        ArrayNode normalized = mapper.createArrayNode();

        if (directAnswers != null && directAnswers.isArray() && !directAnswers.isEmpty()) {
            for (JsonNode answer : directAnswers) {
                String normalizedAnswer = normalizeAnswerToken(answer == null ? null : answer.asText());
                if (normalizedAnswer != null) {
                    normalized.add(normalizedAnswer);
                }
            }
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }

        // Fallback: derive the true answer from each generated question.
        for (JsonNode questionNode : contenuNode) {
            JsonNode embeddedAnswer = firstPresent(questionNode,
                    "vraieReponse",
                    "vraiesReponses",
                    "bonneReponse",
                    "bonne_reponse",
                    "correctAnswer",
                    "reponseCorrecte");

            String normalizedAnswer = normalizeAnswerToken(embeddedAnswer == null ? null : embeddedAnswer.asText());
            if (normalizedAnswer != null) {
                normalized.add(normalizedAnswer);
            }
        }

        return normalized;
    }

    private ArrayNode recoverAnswersWithSecondPass(JsonNode contenuNode, ObjectMapper mapper, String normalizedApiKey) {
        try {
            String prompt = """
                    Tu reçois un tableau JSON de questions QCM.
                    Retourne UNIQUEMENT un JSON avec ce format exact:
                    {\"vraiesReponses\":[\"A\",\"B\",...]}.
                    Contraintes:
                    - Une réponse par question.
                    - Chaque réponse doit être uniquement A, B, C ou D.

                    Questions:
                    %s
                    """.formatted(contenuNode.toString());

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + normalizedApiKey);
            headers.set("HTTP-Referer", "http://localhost");
            headers.set("X-Title", "Recruti");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

            if (response.getBody() == null || response.getBody().get("choices") == null) {
                return mapper.createArrayNode();
            }

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            if (choices.isEmpty() || choices.get(0).get("message") == null) {
                return mapper.createArrayNode();
            }

            Map message = (Map) choices.get(0).get("message");
            String content = (String) message.get("content");
            if (content == null || content.trim().isEmpty()) {
                return mapper.createArrayNode();
            }

            JsonNode parsed = mapper.readTree(extractJsonPayload(content));
            return extractAnswers(parsed, contenuNode, mapper);
        } catch (Exception ignored) {
            return mapper.createArrayNode();
        }
    }

    private ArrayNode normalizeAnswersSize(ArrayNode answers, int expectedSize, ObjectMapper mapper) {
        ArrayNode normalized = mapper.createArrayNode();

        int index = 0;
        for (JsonNode answer : answers) {
            if (index >= expectedSize) {
                break;
            }
            String token = normalizeAnswerToken(answer == null ? null : answer.asText());
            normalized.add(token == null ? "A" : token);
            index++;
        }

        while (normalized.size() < expectedSize) {
            normalized.add("A");
        }

        return normalized;
    }

    private JsonNode firstPresent(JsonNode node, String... fieldNames) {
        if (node == null || fieldNames == null) {
            return null;
        }
        for (String name : fieldNames) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private String normalizeAnswerToken(String raw) {
        if (raw == null) {
            return null;
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // Supports "A", "A.", "A) ...", and numeric forms like "0".."3".
        char first = Character.toUpperCase(trimmed.charAt(0));
        if (first >= 'A' && first <= 'D') {
            return String.valueOf(first);
        }
        if (first >= '1' && first <= '4' && trimmed.length() == 1) {
            return String.valueOf((char) ('A' + (first - '1')));
        }
        if (first >= '0' && first <= '3' && trimmed.length() == 1) {
            return String.valueOf((char) ('A' + (first - '0')));
        }

        return null;
    }

    private String extractJsonPayload(String aiContent) {
        String trimmed = aiContent.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private String normalizeApiKey(String rawApiKey) {
        if (rawApiKey == null) {
            return null;
        }

        String trimmed = rawApiKey.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        int secondPrefix = trimmed.indexOf("sk-or-v1-", "sk-or-v1-".length());
        if (trimmed.startsWith("sk-or-v1-") && secondPrefix > 0) {
            return trimmed.substring(0, secondPrefix);
        }

        return trimmed;
    }
}