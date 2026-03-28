package tn.recruti.recruti_backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import tn.recruti.recruti_backend.Exception.RessourceNotFoundException;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.repository.OfferRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offerRepository;


    public List<Offer> getAllOffers(){
        return offerRepository.findAll();
    }


    public Offer getOfferById(Long id){
        return offerRepository.findById(id)     // findById pas getById
                .orElseThrow(() -> new RessourceNotFoundException("Offer not found : " + id));
    }

    public Offer updateOffer(Long id, Offer updateOffer) {
        Offer offer = getOfferById(id);

        offer.setTitre(updateOffer.getTitre());
        offer.setDescription(updateOffer.getDescription());
        offer.setTags(updateOffer.getTags());
        offer.setAvailable(updateOffer.isAvailable());
        offer.setDateEmission(updateOffer.getDateEmission());
        offer.setQuiz(updateOffer.getQuiz());

        return offerRepository.save(offer);
    }


    public Offer addOffer(Offer offre){
        return offerRepository.save(offre);
    }

    public void deleteOfferById(Long id){
        Offer offer=getOfferById(id);
        offerRepository.delete(offer);
    }


    public List<Offer> getOffersByTitre(String titre) {
        return offerRepository.findByTitreContaining(titre);   // recherche partielle
    }

    public List<Offer> getOffersByAvailable(boolean available) {
        return offerRepository.findByAvailable(available);
    }

    public List<Offer> getOffersByDateEmission(LocalDate dateEmission) {
        return offerRepository.findByDateEmission(dateEmission);
    }

    public List<Offer> getOffersByDateEmissionBetween(LocalDate debut, LocalDate fin) {
        return offerRepository.findByDateEmissionBetween(debut, fin);
    }




}
