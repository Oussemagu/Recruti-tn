package tn.recruti.recruti_backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatbotAskResponse {
    private String answer;
    private List<String> suggestedUrls;
}
