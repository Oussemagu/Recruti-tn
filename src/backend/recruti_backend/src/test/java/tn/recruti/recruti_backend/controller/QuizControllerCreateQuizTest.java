package tn.recruti.recruti_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import tn.recruti.recruti_backend.dto.QuizCreateDto;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.repository.CandidatureRepository;
import tn.recruti.recruti_backend.repository.OfferRepository;
import tn.recruti.recruti_backend.repository.PassageRepository;
import tn.recruti.recruti_backend.repository.QuizRepository;
import tn.recruti.recruti_backend.repository.UserRepository;
import tn.recruti.recruti_backend.service.QuizAiService;

@ExtendWith(MockitoExtension.class)
class QuizControllerCreateQuizTest {

    @InjectMocks
    private QuizController quizController;

    @Mock
    private QuizAiService aiService;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private PassageRepository passageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @Test
    void createQuiz_manualMode_usesPayloadValues() throws Exception {
        QuizCreateDto dto = new QuizCreateDto();
        dto.setMethode(0);
        dto.setOfferId(1L);
        dto.setContenu("[{\"question\":\"Q manuel\"}]");
        dto.setVraiesReponses("[\"A\"]");

        Offer offer = new Offer();
        offer.setId(1L);

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = quizController.createQuiz(dto);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody() instanceof QuizController.QuizResponseDto);

        QuizController.QuizResponseDto body = (QuizController.QuizResponseDto) response.getBody();
        assertNotNull(body);
        assertEquals("[{\"question\":\"Q manuel\"}]", body.getContenu());
        assertEquals("[\"A\"]", body.getVraiesReponses());

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());
        assertEquals("[{\"question\":\"Q manuel\"}]", offerCaptor.getValue().getQuiz().getContenu());
        assertEquals("[\"A\"]", offerCaptor.getValue().getQuiz().getVraiesReponses());
        verify(aiService, never()).generateQuizFromOffre(any(), any(Integer.class));
    }

    @Test
    void createQuiz_aiMode_usesGeneratedValues() throws Exception {
        QuizCreateDto dto = new QuizCreateDto();
        dto.setMethode(1);
        dto.setOfferId(2L);
        dto.setContenu("manual should not be used");
        dto.setVraiesReponses("manual should not be used");

        Offer offer = new Offer();
        offer.setId(2L);
        offer.setDescription("Java backend offer");

        String generatedContenu = "[{\"question\":\"Q IA\",\"choix\":[\"A\",\"B\",\"C\",\"D\"]}]";
        String generatedAnswers = "[\"C\"]";

        when(offerRepository.findById(2L)).thenReturn(Optional.of(offer));
        when(aiService.generateQuizFromOffre(eq("Java backend offer"), eq(5)))
                .thenReturn(Map.of("contenu", generatedContenu, "vraiesReponses", generatedAnswers));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = quizController.createQuiz(dto);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody() instanceof QuizController.QuizResponseDto);

        QuizController.QuizResponseDto body = (QuizController.QuizResponseDto) response.getBody();
        assertNotNull(body);
        assertEquals(generatedContenu, body.getContenu());
        assertEquals(generatedAnswers, body.getVraiesReponses());

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());
        assertEquals(generatedContenu, offerCaptor.getValue().getQuiz().getContenu());
        assertEquals(generatedAnswers, offerCaptor.getValue().getQuiz().getVraiesReponses());
    }

    @Test
    void createQuiz_aiMode_returns502WhenAiProviderFails() throws Exception {
        QuizCreateDto dto = new QuizCreateDto();
        dto.setMethode(1);
        dto.setOfferId(3L);

        Offer offer = new Offer();
        offer.setId(3L);
        offer.setDescription("Spring backend offer");

        when(offerRepository.findById(3L)).thenReturn(Optional.of(offer));
        when(aiService.generateQuizFromOffre(eq("Spring backend offer"), eq(5)))
                .thenThrow(new IllegalStateException("AI provider unauthorized (invalid API key)"));

        ResponseEntity<?> response = quizController.createQuiz(dto);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof QuizController.ErrorDto);
        QuizController.ErrorDto body = (QuizController.ErrorDto) response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("unauthorized"));
    }

    @Test
    void createQuiz_aiMode_returns502WhenGeneratedAnswersAreEmpty() throws Exception {
        QuizCreateDto dto = new QuizCreateDto();
        dto.setMethode(1);
        dto.setOfferId(4L);

        Offer offer = new Offer();
        offer.setId(4L);
        offer.setDescription("Data engineer offer");

        when(offerRepository.findById(4L)).thenReturn(Optional.of(offer));
        when(aiService.generateQuizFromOffre(eq("Data engineer offer"), eq(5)))
                .thenReturn(Map.of(
                        "contenu", "[{\"question\":\"Q IA\",\"choix\":[\"A\",\"B\",\"C\",\"D\"]}]",
                        "vraiesReponses", "[]"
                ));

        ResponseEntity<?> response = quizController.createQuiz(dto);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof QuizController.ErrorDto);
        verify(offerRepository, never()).save(any(Offer.class));
    }
}

