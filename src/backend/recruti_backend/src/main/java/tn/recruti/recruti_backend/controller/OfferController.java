package tn.recruti.recruti_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.model.Offer;
import tn.recruti.recruti_backend.service.OfferService;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @PostMapping
    public ResponseEntity<Offer> addOffer(@RequestBody Offer offer) {
        return ResponseEntity.status(201).body(offerService.addOffer(offer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.updateOffer(id, offer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOfferById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/titre/{titre}")
    public ResponseEntity<List<Offer>> getByTitre(@PathVariable String titre) {
        return ResponseEntity.ok(offerService.getOffersByTitre(titre));
    }

    @GetMapping("/available/{available}")
    public ResponseEntity<List<Offer>> getByAvailable(@PathVariable boolean available) {
        return ResponseEntity.ok(offerService.getOffersByAvailable(available));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Offer>> getByDate(@PathVariable String date) {
        return ResponseEntity.ok(offerService.getOffersByDateEmission(LocalDate.parse(date)));
    }

    @GetMapping("/date-between/{debut}/{fin}")
    public ResponseEntity<List<Offer>> getBetweenDates(
            @PathVariable String debut,
            @PathVariable String fin) {
        return ResponseEntity.ok(
                offerService.getOffersByDateEmissionBetween(
                        LocalDate.parse(debut),
                        LocalDate.parse(fin)));
    }
}