import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { OfferService } from '../../services/offer.service';
import { CandidatureService } from '../../services/candidature.service';
import { QuizService } from '../../services/quiz.service';
import { AuthService } from '../../core/services/auth';
import { Offer } from '../../models/offer.model';
import { Candidature } from '../../models/candidature.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-all-offers',
  imports: [CommonModule, ReactiveFormsModule, FormsModule, DatePipe],
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

  // ── Modal : prendre le quiz ────────────────────────────
  showQuizModal = false;
  selectedQuizOffer: Offer | null = null;
  quizQuestions: Array<{ question: string; choix: string[] }> = [];
  quizAnswers: string[] = [];
  quizLoading = false;
  quizSubmitting = false;
  quizResult: any = null;
  showQuizResult = false;

  constructor(
    private readonly offerService: OfferService,
    public  readonly candidatureService: CandidatureService,
    private readonly quizService: QuizService
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

  hasBeenInvitedToQuiz(offerId: number): boolean {
    const candidature = this.candidatures.find(c => c.idOffre === offerId);
    return candidature ? candidature.invitedToQuiz === true : false;
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

  // ── Modal : Prendre le quiz ───────────────────────────
  ouvrirQuizModal(offer: Offer): void {
    if (!offer.id) return;
    this.selectedQuizOffer = offer;
    this.quizLoading = true;
    this.quizAnswers = [];
    this.quizResult = null;
    this.showQuizResult = false;
    this.showQuizModal = true;
    this.chargerQuiz(offer.id);
  }

  fermerQuizModal(): void {
    this.showQuizModal = false;
    this.selectedQuizOffer = null;
    this.quizQuestions = [];
    this.quizAnswers = [];
    this.quizResult = null;
    this.showQuizResult = false;
  }

  chargerQuiz(offerId: number): void {
    this.quizLoading = true;
    this.errorMessage = '';

    this.quizService.getQuizByOffer(offerId).subscribe({
      next: (quiz) => {
        try {
          const contenu = JSON.parse(quiz.contenu ?? '[]');
          this.quizQuestions = Array.isArray(contenu) ? contenu : [];
          
          // Initialize answers array
          this.quizAnswers = new Array(this.quizQuestions.length).fill('');
          
          this.quizLoading = false;
        } catch (e) {
          this.errorMessage = 'Erreur lors du chargement du quiz';
          this.quizLoading = false;
        }
      },
      error: (err) => {
        if (err.status === 404) {
          this.errorMessage = 'Aucun quiz n\'est associé à cette offre';
        } else {
          this.errorMessage = 'Erreur lors du chargement du quiz';
        }
        this.quizLoading = false;
      }
    });
  }

  soumettreQuiz(): void {
    if (!this.selectedQuizOffer) {
      this.errorMessage = 'Erreur: Offre non sélectionnée';
      return;
    }

    const candidatId = this.getCandidatId();
    if (!candidatId) {
      this.errorMessage = 'Erreur: ID candidat non trouvé';
      return;
    }

    // Check that all questions are answered
    if (this.quizAnswers.some(answer => !answer || answer.trim() === '')) {
      this.errorMessage = 'Veuillez répondre à toutes les questions';
      return;
    }

    this.quizSubmitting = true;
    this.errorMessage = '';

    // Get the quiz first to get its ID
    if (!this.selectedQuizOffer?.id) {
      this.errorMessage = 'Erreur: Offre non sélectionnée';
      this.quizSubmitting = false;
      return;
    }

    this.quizService.getQuizByOffer(this.selectedQuizOffer.id).subscribe({
      next: (quiz) => {
        const payload = {
          quizId: quiz.id,
          candidatId: candidatId,
          answers: this.quizAnswers
        };

        this.quizService.submitQuiz(payload).subscribe({
          next: (result) => {
            this.quizResult = result;
            this.showQuizResult = true;
            this.quizSubmitting = false;
            // Reload data after submission
            setTimeout(() => this.loadData(), 2000);
          },
          error: (err) => {
            this.errorMessage = err.error?.message || 'Erreur lors de la soumission du quiz';
            this.quizSubmitting = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement du quiz';
        this.quizSubmitting = false;
      }
    });
  }

  // ── Navigation ────────────────────────────────────────
  goToProfile(): void {
    this.router.navigate(['/profile']);
  }
}
