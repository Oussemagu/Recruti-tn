import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { OfferService } from '../../services/offer.service';
import { CandidatureService } from '../../services/candidature.service';
import { AuthService } from '../../core/services/auth';
import { Offer } from '../../models/offer.model';
import { Candidature } from '../../models/candidature.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-all-offers',
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  templateUrl: './all-offers.component.html',
  styleUrl: './all-offers.component.css'
})
export class AllOffersComponent implements OnInit {

  private readonly fb        = inject(FormBuilder);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly router    = inject(Router);
  private readonly http      = inject(HttpClient);
  auth                       = inject(AuthService);

  // ── Data ──────────────────────────────────────────────
  allOffers:    Offer[]        = [];
  offers:       Offer[]        = [];
  candidatures: Candidature[]  = [];
  isLoading     = false;
  errorMessage  = '';

  // ── Pagination ────────────────────────────────────────
  currentPage  = 1;
  totalPages   = 1;
  totalOffers  = 0;
  readonly limit = 6;

  // ── Filter form ───────────────────────────────────────
  readonly filterForm = this.fb.group({ titre: [''] });

  // ── Modal : postuler ──────────────────────────────────
  showModal     = false;
  selectedOffer: Offer | null = null;
  selectedFile:  File | null  = null;
  isSubmitting  = false;
  submitSuccess = '';
  submitError   = '';

  // ── Modal : modifier CV ───────────────────────────────
  showEditModal   = false;
  editCandidatureId: number | null = null;
  editFile:  File | null = null;
  isEditing  = false;
  editSuccess = '';
  editError   = '';

  // ── Modal : confirmer suppression ────────────────────
  showDeleteModal       = false;
  deleteCandidatureId: number | null = null;
  isDeleting            = false;

  // ── Modal : détails ───────────────────────────────────
  showDetailsModal    = false;
  selectedCandidature: Candidature | null = null;
  cvUrl: SafeResourceUrl | null = null;

  constructor(
    private readonly offerService: OfferService,
    public  readonly candidatureService: CandidatureService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  // ── Helpers ───────────────────────────────────────────
  private getCandidatId(): number {
    const raw = localStorage.getItem('recruiti_user');
    if (!raw) return 0;
    return JSON.parse(raw)?.id ?? 0;
  }

  isCandidat(): boolean {
    const raw = localStorage.getItem('recruiti_user');
    return raw ? JSON.parse(raw)?.role === 'CANDIDAT' : false;
  }

  hasApplied(offerId: number): boolean {
    return this.candidatures.some(c => c.idOffre === offerId);
  }

  private getCandidatureByOffre(offerId: number): Candidature | undefined {
    return this.candidatures.find(c => c.idOffre === offerId);
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      INITIAL:  'En attente',
      EN_COURS: 'En cours',
      ACCEPTE:  'Accepté',
      REFUSE:   'Refusé'
    };
    return map[status] ?? status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      INITIAL:  'status-pending',
      EN_COURS: 'status-progress',
      ACCEPTE:  'status-accepted',
      REFUSE:   'status-rejected'
    };
    return map[status] ?? 'status-pending';
  }

  // ── Load ──────────────────────────────────────────────
  loadData(): void {
    this.isLoading   = true;
    this.errorMessage = '';
    const candidatId = this.getCandidatId();

    forkJoin({
      paged:        this.offerService.getOffers(this.currentPage, this.limit),
      candidatures: this.candidatureService.getCandidaturesByCandidat(candidatId)
    }).subscribe({
      next: ({ paged, candidatures }) => {
        this.allOffers   = paged.data;
        this.offers      = paged.data;
        this.totalPages  = paged.totalPages;
        this.totalOffers = paged.total;
        this.candidatures = candidatures;
        this.isLoading   = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement des données.';
        this.isLoading    = false;
      }
    });
  }

  // ── Pagination ────────────────────────────────────────
  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.loadData();
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  // ── Filters ───────────────────────────────────────────
  applyFilters(): void {
    const term = (this.filterForm.value.titre ?? '').trim().toLowerCase();
    this.offers = term
      ? this.allOffers.filter(o => o.titre.toLowerCase().includes(term))
      : this.allOffers;
  }

  resetFilters(): void {
    this.filterForm.reset({ titre: '' });
    this.offers = this.allOffers;
  }

  // ── Modal : Postuler ──────────────────────────────────
  ouvrirModal(offer: Offer): void {
    this.selectedOffer  = offer;
    this.selectedFile   = null;
    this.submitSuccess  = '';
    this.submitError    = '';
    this.showModal      = true;
  }

  fermerModal(): void {
    this.showModal     = false;
    this.selectedOffer = null;
    this.selectedFile  = null;
    this.submitSuccess = '';
    this.submitError   = '';
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
    }
  }

  soumettreCandidature(): void {
    if (!this.selectedFile || !this.selectedOffer?.id) return;

    const candidatId = this.getCandidatId();
    const offreId    = Number(this.selectedOffer.id);

    this.isSubmitting  = true;
    this.submitError   = '';
    this.submitSuccess = '';

    this.candidatureService.postuler(candidatId, offreId, this.selectedFile).subscribe({
      next: (msg) => {
        this.submitSuccess = msg;
        this.isSubmitting  = false;
        setTimeout(() => { this.fermerModal(); this.loadData(); }, 1500);
      },
      error: (err) => {
        this.submitError  = err.error || 'Erreur lors de la candidature.';
        this.isSubmitting = false;
      }
    });
  }

  // ── Modal : Modifier CV ───────────────────────────────
  ouvrirModalModifier(offerId: number): void {
    const candidature = this.getCandidatureByOffre(offerId);
    if (!candidature) return;
    this.editCandidatureId = candidature.idCandidature;
    this.editFile          = null;
    this.editSuccess       = '';
    this.editError         = '';
    this.showEditModal     = true;
  }

  fermerModalModifier(): void {
    this.showEditModal     = false;
    this.editCandidatureId = null;
    this.editFile          = null;
    this.editSuccess       = '';
    this.editError         = '';
  }

  onEditFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.editFile = input.files[0];
    }
  }

  soumettreModification(): void {
    if (!this.editFile || this.editCandidatureId === null) return;

    this.isEditing  = true;
    this.editError  = '';
    this.editSuccess = '';

    const formData = new FormData();
    formData.append('cv', this.editFile, this.editFile.name);

    this.http.patch(
      `${environment.apiUrl}/api/candidatures/${this.editCandidatureId}`,
      formData,
      { responseType: 'json' }
    ).subscribe({
      next: () => {
        this.editSuccess = 'CV mis à jour avec succès.';
        this.isEditing   = false;
        setTimeout(() => { this.fermerModalModifier(); this.loadData(); }, 1500);
      },
      error: (err) => {
        this.editError = err.error?.message || 'Erreur lors de la mise à jour du CV.';
        this.isEditing = false;
      }
    });
  }

  // ── Modal : Suppression ───────────────────────────────
  ouvrirModalSuppression(offerId: number): void {
    const candidature = this.getCandidatureByOffre(offerId);
    if (!candidature) return;
    this.deleteCandidatureId = candidature.idCandidature;
    this.showDeleteModal     = true;
  }

  fermerModalSuppression(): void {
    this.showDeleteModal     = false;
    this.deleteCandidatureId = null;
  }

  confirmerSuppression(): void {
    if (this.deleteCandidatureId === null) return;
    this.isDeleting = true;

    this.candidatureService.deleteCandidature(this.deleteCandidatureId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.fermerModalSuppression();
        this.loadData();
      },
      error: () => {
        this.isDeleting = false;
        this.fermerModalSuppression();
        // optionally show an error toast
      }
    });
  }

  // ── Modal : Détails ───────────────────────────────────
  consulterCandidature(offerId: number): void {
    const candidature = this.getCandidatureByOffre(offerId);
    if (!candidature) return;
    this.selectedCandidature = candidature;
    const url = this.candidatureService.getCvUrl(candidature.cvPath);
    this.cvUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    this.showDetailsModal = true;
  }

  fermerDetailsModal(): void {
    this.showDetailsModal    = false;
    this.selectedCandidature = null;
    this.cvUrl               = null;
  }

  // ── Navigation ────────────────────────────────────────
  goToProfile(): void {
    this.router.navigate(['/profile']);
  }
}
