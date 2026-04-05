package tn.recruti.recruti_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.dto.QuizCreateDto;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.model.Passage;
import tn.recruti.recruti_backend.model.Quiz;
import tn.recruti.recruti_backend.repository.OfferRepository;
import tn.recruti.recruti_backend.repository.QuizRepository;

import java.util.ArrayList;
import java.util.Optional;

@RestController
public class QuizController {

    @Autowired
    OfferRepository offerRepository;

    @Autowired
    QuizRepository quizRepository;

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



}
