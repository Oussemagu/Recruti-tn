package tn.recruti.recruti_backend.dto.chatbot;

import lombok.Data;

@Data
public class ChatbotAskRequest {
    private String message;
    private String currentPath;
}
