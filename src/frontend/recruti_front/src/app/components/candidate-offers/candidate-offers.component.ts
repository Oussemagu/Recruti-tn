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
  applications: Candidature[] = [];  // Keep as 'applications' for the template
  isLoading = false;
  errorMessage = '';

  // Application modal
  showModal = false;
  selectedOffer: Offer | null = null;
  selectedFile: File | null = null;
  isSubmitting = false;
  submitSuccess = '';
  submitError = '';

  // Details modal
  showDetailsModal = false;
  selectedApplication: Candidature | null = null;  // Keep as 'selectedApplication' for the template
  cvUrl: SafeResourceUrl | null = null;

  readonly filterForm = this.fb.group({
    title: [''],           // Changed from 'titre'
    startDate: [''],       // Changed from 'dateDebut'
    endDate: ['']          // Changed from 'dateFin'
  });

  constructor(
    private readonly offerService: OfferService,
    public readonly applicationService: CandidatureService  // Keep as 'applicationService' for the template
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  private getCandidateId(): number {  // Changed from getCandidatId
    const stored = localStorage.getItem('userId');
    const parsed = Number(stored);
    return stored && !isNaN(parsed) ? parsed : 1;
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    const candidateId = this.getCandidateId();

    forkJoin({
      offers: this.offerService.filterByAvailable(true),
      applications: this.applicationService.getCandidaturesByCandidat(candidateId)
    }).subscribe({
      next: (result) => {
        this.offers = result.offers;
        this.applications = result.applications;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Error loading data.';
        this.isLoading = false;
      }
    });
  }

  hasApplied(offerId: number): boolean {
    return this.applications.some(c => c.idOffre === offerId);
  }

  getApplication(offerId: number): Candidature | undefined {  // Changed from getCandidature
    return this.applications.find(c => c.idOffre === offerId);
  }

  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      'INITIAL': 'Pending',         // Changed from 'En attente'
      'EN_COURS': 'In progress',    // Changed from 'En cours'
      'ACCEPTE': 'Accepted',        // Changed from 'Accepté'
      'REFUSE': 'Rejected'          // Changed from 'Refusé'
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
    const title = (this.filterForm.value.title ?? '').trim();
    const startDate = this.filterForm.value.startDate ?? '';
    const endDate = this.filterForm.value.endDate ?? '';
    this.isLoading = true;
    this.errorMessage = '';

    if (startDate && endDate) {
      this.offerService.filterByDateRange(startDate, endDate).subscribe({
        next: (data) => {
          const available = data.filter(o => o.available);
          this.offers = title ? available.filter(o => o.titre.toLowerCase().includes(title.toLowerCase())) : available;
          this.isLoading = false;
        },
        error: () => { this.errorMessage = 'Error applying filters.'; this.isLoading = false; }
      });
      return;
    }

    if (title) {
      this.offerService.searchByTitre(title).subscribe({
        next: (data) => { this.offers = data.filter(o => o.available); this.isLoading = false; },
        error: () => { this.errorMessage = 'Error during search.'; this.isLoading = false; }
      });
      return;
    }

    this.loadAvailableOffers();
  }

  resetFilters(): void {
    this.filterForm.reset({ title: '', startDate: '', endDate: '' });
    this.loadAvailableOffers();
  }

  openModal(offer: Offer): void {  // Changed from ouvrirModal
    this.selectedOffer = offer;
    this.selectedFile = null;
    this.submitSuccess = '';
    this.submitError = '';
    this.showModal = true;
  }

  closeModal(): void {  // Changed from fermerModal
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

  submitApplication(): void {  // Changed from soumettreCandidature
    if (!this.selectedFile || !this.selectedOffer || !this.selectedOffer.id) return;

    const candidateId = this.getCandidateId();
    const offerId = Number(this.selectedOffer.id);

    if (isNaN(offerId)) {
      this.submitError = 'Invalid offer ID.';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';
    this.submitSuccess = '';

    this.applicationService.postuler(candidateId, offerId, this.selectedFile).subscribe({
      next: (msg) => {
        this.submitSuccess = msg;
        this.isSubmitting = false;
        setTimeout(() => {
          this.closeModal();
          this.loadData();
        }, 1500);
      },
      error: (err) => {
        this.submitError = err.error || 'Error submitting application.';
        this.isSubmitting = false;
      }
    });
  }

  viewApplication(offerId: number): void {  // Changed from consulterCandidature
    const application = this.getApplication(offerId);
    if (!application) return;

    this.selectedApplication = application;

    // Build CV URL
    const cvUrl = this.applicationService.getCvUrl(application.cvPath);
    this.cvUrl = this.sanitizer.bypassSecurityTrustResourceUrl(cvUrl);

    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {  // Changed from fermerDetailsModal
    this.showDetailsModal = false;
    this.selectedApplication = null;
    this.cvUrl = null;
  }

  deleteApplication(offerId: number): void {  // Changed from supprimerCandidature
    const application = this.getApplication(offerId);
    if (!application) return;

    if (confirm('Are you sure you want to withdraw this application?')) {
      this.applicationService.deleteCandidature(application.idCandidature).subscribe({
        next: () => {
          this.loadData();
        },
        error: () => {
          alert('Error withdrawing application.');
        }
      });
    }
  }
}
