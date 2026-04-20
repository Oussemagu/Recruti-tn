package tn.recruti.recruti_backend.service.chatbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tn.recruti.recruti_backend.dto.chatbot.ChatbotAskResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {

    @Value("${api.key:}")
    private String apiKey;

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openrouter/auto";

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatbotAskResponse ask(String userMessage, String currentPath) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ChatbotAskResponse("Pose-moi une question sur Recruti.tn et je t'aide.", defaultUrls());
        }

        String normalizedApiKey = normalizeApiKey(apiKey);
        if (normalizedApiKey == null || normalizedApiKey.isEmpty()) {
            return new ChatbotAskResponse(
                    "Le chatbot n'est pas configure (api.key manquante).",
                    defaultUrls()
            );
        }

        String systemPrompt = """
                Tu es l'assistant officiel de Recruti.tn.
                Recruti.tn connecte recruteurs et candidats.
                
                Ta mission:
                - Repondre clairement sur le fonctionnement de la plateforme.
                - Aider l'utilisateur a naviguer dans l'application.
                - Quand c'est utile, proposer des URLs internes exactes.
                
                URLs internes disponibles:
                - /auth/login
                - /auth/register
                - /home
                - /candidat/offres
                - /candidat/mes-offres
                - /schedule-interview
                
                Regles:
                - Reponds en francais.
                - Sois concis, utile, et oriente action.
                - N'invente pas des routes qui n'existent pas.
                """;

        String userPrompt = "Page actuelle: " + (currentPath == null ? "inconnue" : currentPath)
                + "\nQuestion utilisateur: " + userMessage;

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + normalizedApiKey);
        headers.set("HTTP-Referer", "http://localhost");
        headers.set("X-Title", "Recruti Chatbot");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
            String answer = extractAnswer(response.getBody());
            List<String> urls = extractKnownUrls(answer);
            if (urls.isEmpty()) {
                urls = defaultUrls();
            }
            return new ChatbotAskResponse(answer, urls);
        } catch (HttpStatusCodeException ex) {
            return new ChatbotAskResponse(
                    "Le service chatbot est temporairement indisponible. Reessaie dans un moment.",
                    defaultUrls()
            );
        } catch (Exception ex) {
            return new ChatbotAskResponse(
                    "Une erreur est survenue pendant la reponse chatbot.",
                    defaultUrls()
            );
        }
    }

    private String extractAnswer(Map body) {
        if (body == null || body.get("choices") == null) {
            return "Je n'ai pas pu generer une reponse pour le moment.";
        }

        List<Map> choices = (List<Map>) body.get("choices");
        if (choices.isEmpty() || choices.get(0).get("message") == null) {
            return "Je n'ai pas pu generer une reponse pour le moment.";
        }

        Map message = (Map) choices.get(0).get("message");
        String content = (String) message.get("content");
        if (content == null || content.trim().isEmpty()) {
            return "Je n'ai pas pu generer une reponse pour le moment.";
        }

        return content;
    }

    private List<String> extractKnownUrls(String answer) {
        List<String> extracted = new ArrayList<>();
        if (answer == null || answer.isBlank()) {
            return extracted;
        }

        Pattern pattern = Pattern.compile("/(auth/login|auth/register|home|candidat/offres|candidat/mes-offres|schedule-interview)");
        Matcher matcher = pattern.matcher(answer);
        while (matcher.find()) {
            String url = matcher.group();
            if (!extracted.contains(url)) {
                extracted.add(url);
            }
        }
        return extracted;
    }

    private List<String> defaultUrls() {
        return List.of("/home", "/auth/login", "/auth/register", "/candidat/offres", "/candidat/mes-offres", "/schedule-interview");
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
