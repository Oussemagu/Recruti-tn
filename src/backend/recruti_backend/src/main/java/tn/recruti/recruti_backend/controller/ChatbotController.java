package tn.recruti.recruti_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.recruti.recruti_backend.dto.chatbot.ChatbotAskRequest;
import tn.recruti.recruti_backend.dto.chatbot.ChatbotAskResponse;
import tn.recruti.recruti_backend.service.chatbot.ChatbotService;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatbotAskResponse> ask(@RequestBody ChatbotAskRequest request) {
        String message = request == null ? null : request.getMessage();
        String currentPath = request == null ? null : request.getCurrentPath();
        return ResponseEntity.ok(chatbotService.ask(message, currentPath));
    }
}
