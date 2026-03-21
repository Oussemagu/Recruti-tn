// candidate-offers.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { OfferService } from '../../services/offer.service';
import { CandidatureService } from '../../services/candidature.service';
import { Offer } from '../../models/offer.model';
import { Candidature } from '../../models/candidature.model';
import { forkJoin } from 'rxjs';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-candidate-offers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './candidate-offers.component.html',
  styleUrl: './candidate-offers.component.css'
})
export class CandidateOffersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly sanitizer = inject(DomSanitizer);

  offers: Offer[] = [];
  candidatures: Candidature[] = [];
  isLoading = false;
  errorMessage = '';

  // Modal de postulation
  showModal = false;
  selectedOffer: Offer | null = null;
  selectedFile: File | null = null;
  isSubmitting = false;
  submitSuccess = '';
  submitError = '';

  // Modal de consultation
  showDetailsModal = false;
  selectedCandidature: Candidature | null = null;
  cvUrl: SafeResourceUrl | null = null;

  readonly filterForm = this.fb.group({
    titre: [''],
    dateDebut: [''],
    dateFin: ['']
  });

  constructor(
    private readonly offerService: OfferService,
    public readonly candidatureService: CandidatureService  // ✅ Changé de private à public
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  private getCandidatId(): number {
    const stored = localStorage.getItem('userId');
    const parsed = Number(stored);
    return stored && !isNaN(parsed) ? parsed : 1;
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    const candidatId = this.getCandidatId();

    forkJoin({
      offers: this.offerService.filterByAvailable(true),
      candidatures: this.candidatureService.getCandidaturesByCandidat(candidatId)
    }).subscribe({
      next: (result) => {
        this.offers = result.offers;
        this.candidatures = result.candidatures;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement des données.';
        this.isLoading = false;
      }
    });
  }

  hasApplied(offerId: number): boolean {
    return this.candidatures.some(c => c.idOffre === offerId);
  }

  getCandidature(offerId: number): Candidature | undefined {
    return this.candidatures.find(c => c.idOffre === offerId);
  }

  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      'INITIAL': 'En attente',
      'EN_COURS': 'En cours',
      'ACCEPTE': 'Accepté',
      'REFUSE': 'Refusé'
    };
    return statusMap[status] || status;
  }

  getStatusClass(status: string): string {
    const classMap: { [key: string]: string } = {
      'INITIAL': 'status-pending',
      'EN_COURS': 'status-progress',
      'ACCEPTE': 'status-accepted',
      'REFUSE': 'status-rejected'
    };
    return classMap[status] || 'status-pending';
  }

  loadAvailableOffers(): void {
    this.loadData();
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
          const available = data.filter(o => o.available);
          this.offers = titre ? available.filter(o => o.titre.toLowerCase().includes(titre.toLowerCase())) : available;
          this.isLoading = false;
        },
        error: () => { this.errorMessage = 'Erreur lors du filtrage.'; this.isLoading = false; }
      });
      return;
    }

    if (titre) {
      this.offerService.searchByTitre(titre).subscribe({
        next: (data) => { this.offers = data.filter(o => o.available); this.isLoading = false; },
        error: () => { this.errorMessage = 'Erreur lors de la recherche.'; this.isLoading = false; }
      });
      return;
    }

    this.loadAvailableOffers();
  }

  resetFilters(): void {
    this.filterForm.reset({ titre: '', dateDebut: '', dateFin: '' });
    this.loadAvailableOffers();
  }

  ouvrirModal(offer: Offer): void {
    this.selectedOffer = offer;
    this.selectedFile = null;
    this.submitSuccess = '';
    this.submitError = '';
    this.showModal = true;
  }

  fermerModal(): void {
    this.showModal = false;
    this.selectedOffer = null;
    this.selectedFile = null;
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  soumettreCandidature(): void {
    if (!this.selectedFile || !this.selectedOffer || !this.selectedOffer.id) return;

    const candidatId = this.getCandidatId();
    const offreId = Number(this.selectedOffer.id);

    if (isNaN(offreId)) {
      this.submitError = 'ID offre invalide.';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';
    this.submitSuccess = '';

    this.candidatureService.postuler(candidatId, offreId, this.selectedFile).subscribe({
      next: (msg) => {
        this.submitSuccess = msg;
        this.isSubmitting = false;
        setTimeout(() => {
          this.fermerModal();
          this.loadData();
        }, 1500);
      },
      error: (err) => {
        this.submitError = err.error || 'Erreur lors de la candidature.';
        this.isSubmitting = false;
      }
    });
  }

  consulterCandidature(offerId: number): void {
    const candidature = this.getCandidature(offerId);
    if (!candidature) return;

    this.selectedCandidature = candidature;

    // Construire l'URL du CV
    const cvUrl = this.candidatureService.getCvUrl(candidature.cvPath);
    this.cvUrl = this.sanitizer.bypassSecurityTrustResourceUrl(cvUrl);

    this.showDetailsModal = true;
  }

  fermerDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedCandidature = null;
    this.cvUrl = null;
  }

  supprimerCandidature(offerId: number): void {
    const candidature = this.getCandidature(offerId);
    if (!candidature) return;

    if (confirm('Êtes-vous sûr de vouloir supprimer cette candidature ?')) {
      this.candidatureService.deleteCandidature(candidature.idCandidature).subscribe({
        next: () => {
          this.loadData();
        },
        error: () => {
          alert('Erreur lors de la suppression de la candidature.');
        }
      });
    }
  }
}
