import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { OfferService } from '../../services/offer.service';
import { Offer } from '../../models/offer.model';

@Component({
  selector: 'app-candidate-offers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './candidate-offers.component.html',
  styleUrl: './candidate-offers.component.css'
})
export class CandidateOffersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  offers: Offer[] = [];
  isLoading = false;
  errorMessage = '';

  readonly filterForm = this.fb.group({
    titre: [''],
    dateDebut: [''],
    dateFin: ['']
  });

  constructor(private readonly offerService: OfferService) {}

  ngOnInit(): void {
    this.loadAvailableOffers();
  }

  loadAvailableOffers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.offerService.filterByAvailable(true).subscribe({
      next: (data) => {
        this.offers = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement des offres disponibles.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    const titre = (this.filterForm.value.titre ?? '').trim();
    const dateDebut = this.filterForm.value.dateDebut ?? '';
    const dateFin = this.filterForm.value.dateFin ?? '';

    this.isLoading = true;
    this.errorMessage = '';

    if (dateDebut && dateFin) {
      this.offerService.filterByDateRange(dateDebut, dateFin).subscribe({
        next: (data) => {
          const availableOffers = data.filter((o) => o.available);
          this.offers = titre
            ? availableOffers.filter((o) => o.titre.toLowerCase().includes(titre.toLowerCase()))
            : availableOffers;
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors du filtrage par intervalle de dates.';
          this.isLoading = false;
        }
      });
      return;
    }

    if (titre) {
      this.offerService.searchByTitre(titre).subscribe({
        next: (data) => {
          this.offers = data.filter((o) => o.available);
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors de la recherche par titre.';
          this.isLoading = false;
        }
      });
      return;
    }

    this.loadAvailableOffers();
  }

  resetFilters(): void {
    this.filterForm.reset({
      titre: '',
      dateDebut: '',
      dateFin: ''
    });
    this.loadAvailableOffers();
  }

  postuler(offer: Offer): void {
    window.alert(`Candidature UI: Vous avez cliqué sur "Postuler" pour l'offre "${offer.titre}".`);
  }
}