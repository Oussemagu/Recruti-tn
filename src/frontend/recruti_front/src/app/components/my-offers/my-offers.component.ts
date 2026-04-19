import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth';
import { OfferService,OfferResponse, OfferRequest  } from '../../services/offer.service';
import { CandidatureService } from '../../services/candidature.service';
import { PagedResponse } from '../../models/paged-response.model';
import { QuizService } from '../../services/quiz.service';
import { QuizCreateRequest, PassageWithCandidate } from '../../models/quiz.model';

type QuizQuestionForm = {
  question: string;
  choix: string[];
  vraieReponse: string;
};

@Component({
  selector: 'app-my-offers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './my-offers.component.html',
  styleUrl: './my-offers.component.css'
})
export class MyOffersComponent implements OnInit {

  // Signals
  offers = signal<OfferResponse[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  // Pagination
  currentPage = signal<number>(1);
  totalPages = signal<number>(1);
  totalElements = signal<number>(0);
  limit = 6;

  // Modals
  showAddModal = signal<boolean>(false);
  showEditModal = signal<boolean>(false);
  showDeleteModal = signal<boolean>(false);
  showCandidaturesModal = signal<boolean>(false);
  showQuizModal = signal<boolean>(false);
  showInterviewsModal = signal<boolean>(false);

  // Candidatures
  candidatures = signal<any[]>([]);
  candidaturesLoading = signal<boolean>(false);

  // Interviews
  passages = signal<PassageWithCandidate[]>([]);
  passagesLoading = signal<boolean>(false);

  // Quiz
  quizLoading = signal<boolean>(false);
  quizSubmitting = signal<boolean>(false);
  selectedQuizId = signal<number | null>(null);
  quizCreationMode = signal<'none' | 'manual' | 'ai'>('none');
  quizQuestionsForm: QuizQuestionForm[] = [];
  aiQuestionsCount = 5;

  // Formulaire
  formData = {
    titre: '',
    description: '',
    tags: '',
    available: true
  };

  selectedOffer: OfferResponse | null = null;

  recruiterId: number | null = null;

  constructor(
    private authService: AuthService,
    private offerService: OfferService,
    private candidatureService: CandidatureService,
    private quizService: QuizService
  ) {}

  ngOnInit(): void {
    // Récupérer l'ID du recruteur depuis le signal
    this.recruiterId = this.authService.userId();

    if (!this.recruiterId) {
      this.error.set('Erreur: ID du recruteur non trouvé');
      return;
    }

    this.loadOffers();
  }

  /**
   * Charge les offres du recruteur
   */
  loadOffers(): void {
    if (!this.recruiterId) return;

    this.loading.set(true);
    this.error.set(null);

    this.offerService.getMyOffers(this.recruiterId, this.currentPage(), this.limit)
      .subscribe({
        next: (response: PagedResponse<OfferResponse>) => {
          // Adapter à votre structure PagedResponse: data, page, limit, total, totalPages
          this.offers.set(response.data);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.total);
          this.loading.set(false);
        },
        error: (err: any) => {
          this.error.set('Erreur lors du chargement des offres');
          this.loading.set(false);
          console.error(err);
        }
      });
  }

  /**
   * Ouvre le modal d'ajout d'offre
   */
  openAddModal(): void {
    this.resetForm();
    this.showAddModal.set(true);
  }

  /**
   * Ouvre le modal de modification
   */
  openEditModal(offer: OfferResponse): void {
    this.selectedOffer = offer;
    this.formData = {
      titre: offer.titre,
      description: offer.description,
      tags: offer.tags,
      available: offer.available
    };
    this.showEditModal.set(true);
  }

  /**
   * Ouvre le modal de confirmation de suppression
   */
  openDeleteModal(offer: OfferResponse): void {
    this.selectedOffer = offer;
    this.showDeleteModal.set(true);
  }

  /**
   * Ferme tous les modals
   */
  closeModals(): void {
    this.showAddModal.set(false);
    this.showEditModal.set(false);
    this.showDeleteModal.set(false);
    this.showCandidaturesModal.set(false);
    this.showQuizModal.set(false);
    this.showInterviewsModal.set(false);
    this.selectedOffer = null;
    this.resetForm();
    this.resetQuizForm();
    this.passages.set([]);
  }

  /**
   * Réinitialise le formulaire
   */
  resetForm(): void {
    this.formData = {
      titre: '',
      description: '',
      tags: '',
      available: true
    };
  }

  resetQuizForm(): void {
    this.quizQuestionsForm = [
      {
        question: '',
        choix: ['', ''],
        vraieReponse: ''
      }
    ];
    this.selectedQuizId.set(null);
    this.quizCreationMode.set('none');
    this.aiQuestionsCount = 5;
  }

  addQuestion(): void {
    this.quizQuestionsForm.push({
      question: '',
      choix: ['', ''],
      vraieReponse: ''
    });
  }

  removeQuestion(index: number): void {
    if (this.quizQuestionsForm.length <= 1) return;
    this.quizQuestionsForm.splice(index, 1);
  }

  addChoice(questionIndex: number): void {
    this.quizQuestionsForm[questionIndex].choix.push('');
  }

  removeChoice(questionIndex: number, choiceIndex: number): void {
    const question = this.quizQuestionsForm[questionIndex];
    if (question.choix.length <= 2) return;

    const removedChoice = question.choix[choiceIndex];
    question.choix.splice(choiceIndex, 1);

    if (question.vraieReponse === removedChoice) {
      question.vraieReponse = '';
    }
  }

  /**
   * Ajoute une nouvelle offre
   */
  submitAddOffer(): void {
    if (!this.recruiterId) return;

    this.loading.set(true);
    this.error.set(null);

    const offerRequest: OfferRequest = {
      ...this.formData,
      idRecruteur: this.recruiterId,
      dateEmission: new Date().toISOString()
    };

    this.offerService.addOffer(offerRequest).subscribe({
      next: () => {
        this.success.set('Offre ajoutée avec succès !');
        this.closeModals();
        this.loadOffers();
        setTimeout(() => this.success.set(null), 3000);
      },
      error: (err: any) => {
        if (err.status === 409) {
          this.error.set('Une offre avec ce titre existe déjà');
        } else {
          this.error.set('Erreur lors de l\'ajout de l\'offre');
        }
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  /**
   * Met à jour une offre existante
   */
  submitEditOffer(): void {
    if (!this.selectedOffer || !this.recruiterId) return;

    this.loading.set(true);
    this.error.set(null);

    const offerRequest: OfferRequest = {
      ...this.formData,
      idRecruteur: this.recruiterId,
      dateEmission: this.selectedOffer.dateEmission
    };

    this.offerService.updateOffer(this.selectedOffer.id, offerRequest).subscribe({
      next: () => {
        this.success.set('Offre modifiée avec succès !');
        this.closeModals();
        this.loadOffers();
        setTimeout(() => this.success.set(null), 3000);
      },
      error: (err: any) => {
        this.error.set('Erreur lors de la modification de l\'offre');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  /**
   * Supprime une offre
   */
  confirmDelete(): void {
    if (!this.selectedOffer) return;

    this.loading.set(true);
    this.error.set(null);

    this.offerService.deleteOffer(this.selectedOffer.id).subscribe({
      next: () => {
        this.success.set('Offre supprimée avec succès !');
        this.closeModals();

        // Si on supprime le dernier élément d'une page, retourner à la page précédente
        if (this.offers().length === 1 && this.currentPage() > 1) {
          this.currentPage.set(this.currentPage() - 1);
        }

        this.loadOffers();
        setTimeout(() => this.success.set(null), 3000);
      },
      error: (err: any) => {
        this.error.set('Erreur lors de la suppression de l\'offre');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  /**
   * Navigation pagination
   */
  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages()) return;
    this.currentPage.set(page);
    this.loadOffers();
  }

  previousPage(): void {
    if (this.currentPage() > 1) {
      this.goToPage(this.currentPage() - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.goToPage(this.currentPage() + 1);
    }
  }

  /**
   * Formatte la date
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  /**
   * Ouvre le modal pour voir les candidats d'une offre
   */
  openCandidaturesModal(offer: OfferResponse): void {
    this.selectedOffer = offer;
    this.candidaturesLoading.set(true);
    this.candidatures.set([]);
    this.showCandidaturesModal.set(true);
    this.loadCandidatures(offer.id);
  }

  /**
   * Charge les candidatures pour une offre
   */
  loadCandidatures(offerId: number): void {
    this.candidaturesLoading.set(true);
    this.candidatureService.getCandidaturesByOffer(offerId).subscribe({
      next: (data: any[]) => {
        this.candidatures.set(data);
        this.candidaturesLoading.set(false);
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des candidatures', err);
        this.candidatures.set([]);
        this.candidaturesLoading.set(false);
        this.error.set('Erreur lors du chargement des candidatures');
      }
    });
  }

  /**
   * Ouvre le CV du candidat
   */
  viewCv(candidature: any): void {
    if (candidature.cvPath) {
      const cvUrl = this.candidatureService.getCvUrl(candidature.cvPath);
      window.open(cvUrl, '_blank');
    }
  }

  /**
   * Invite le candidat à prendre le quiz
   */
  inviteToQuiz(candidature: any): void {
    if (!this.selectedOffer) {
      this.error.set('Erreur: Offre non sélectionnée');
      return;
    }

    // First check if there is a quiz for this offer
    this.quizService.getQuizByOffer(this.selectedOffer.id).subscribe({
      next: (quiz) => {
        // Quiz exists, now invite the candidate by updating their candidature in the database
        this.candidatureService.inviteToQuiz(candidature.idCandidature).subscribe({
          next: () => {
            // Mark as invited locally
            candidature.invitedToQuiz = true;
            
            // Show success message
            this.success.set(`Invitation envoyée à ${candidature.candidatPrenom} ${candidature.candidatNom} pour le quiz !`);
            setTimeout(() => this.success.set(null), 3000);
          },
          error: (err) => {
            this.error.set('Erreur lors de l\'invitation au quiz');
            console.error(err);
          }
        });
      },
      error: (err) => {
        if (err.status === 404) {
          this.error.set('Aucun quiz n\'est associé à cette offre. Veuillez en créer un d\'abord.');
        } else {
          this.error.set('Erreur lors de la vérification du quiz');
        }
      }
    });
  }

  /**
   * Ouvre le modal de gestion du quiz lié à l'offre
   */
  openQuizModal(offer: OfferResponse): void {
    this.selectedOffer = offer;
    this.showQuizModal.set(true);
    this.quizCreationMode.set('none');
    this.loadQuizForOffer(offer.id);
  }

  selectQuizCreationMode(mode: 'manual' | 'ai'): void {
    this.quizCreationMode.set(mode);
  }

  generateQuizWithAi(offer: OfferResponse, questionsCount: number): void {
    if (!Number.isInteger(questionsCount) || questionsCount <= 0) {
      this.error.set('Le nombre de questions doit etre un entier strictement positif.');
      return;
    }

    this.selectedOffer = offer;
    this.quizSubmitting.set(true);
    this.error.set(null);

    const payload: QuizCreateRequest = {
      contenu: '',
      vraiesReponses: '',
      offerId: offer.id,
      methode: 1,
      nombreQuestions: questionsCount
    };

    this.quizService.createQuiz(payload).subscribe({
      next: () => {
        this.success.set(`Quiz IA genere avec succes (${questionsCount} question(s)).`);
        this.quizSubmitting.set(false);
        this.quizCreationMode.set('manual');
        this.openQuizModal(offer);
        setTimeout(() => this.success.set(null), 3000);
      },
      error: (err: any) => {
        this.error.set('Erreur lors de la generation du quiz avec IA');
        this.quizSubmitting.set(false);
        console.error(err);
      }
    });
  }

  loadQuizForOffer(offerId: number): void {
    this.quizLoading.set(true);
    this.error.set(null);

    this.quizService.getQuizByOffer(offerId).subscribe({
      next: (quiz) => {
        let contenu: Array<{ question?: string; choix?: string[] }> = [];
        let vraiesReponses: string[] = [];

        try {
          contenu = JSON.parse(quiz.contenu ?? '[]');
        } catch {
          contenu = [];
        }

        try {
          vraiesReponses = JSON.parse(quiz.vraiesReponses ?? '[]');
        } catch {
          vraiesReponses = [];
        }

        this.quizQuestionsForm = (Array.isArray(contenu) ? contenu : []).map((item, index) => ({
          question: item?.question ?? '',
          choix: Array.isArray(item?.choix) && item.choix.length > 0 ? item.choix : ['', ''],
          vraieReponse: this.resolveTrueAnswer(vraiesReponses[index], Array.isArray(item?.choix) && item.choix.length > 0 ? item.choix : ['', ''])
        }));

        if (this.quizQuestionsForm.length === 0) {
          this.resetQuizForm();
        }

        this.selectedQuizId.set(quiz.id);
        this.quizCreationMode.set('manual');
        this.quizLoading.set(false);
      },
      error: (err: any) => {
        if (err.status === 404) {
          this.resetQuizForm();
          this.quizCreationMode.set('none');
        } else {
          this.error.set('Erreur lors du chargement du quiz');
          console.error(err);
        }
        this.quizLoading.set(false);
      }
    });
  }

  submitQuiz(): void {
    if (!this.selectedOffer) return;

    const sanitizedQuestions = this.quizQuestionsForm.map((q) => ({
      question: q.question.trim(),
      choix: q.choix.map((c) => c.trim()).filter((c) => c.length > 0),
      vraieReponse: q.vraieReponse.trim()
    }));

    const hasInvalidQuestion = sanitizedQuestions.some(
      (q) => q.question.length === 0 || q.choix.length < 2 || q.vraieReponse.length === 0 || !q.choix.includes(q.vraieReponse)
    );

    if (hasInvalidQuestion) {
      this.error.set('Chaque question doit avoir un texte, au moins 2 reponses, et une vraie reponse qui existe dans la liste.');
      return;
    }

    this.quizSubmitting.set(true);
    this.error.set(null);

    const contenuPayload = sanitizedQuestions.map((q) => ({
      question: q.question,
      choix: q.choix
    }));

    const vraiesReponsesPayload = sanitizedQuestions.map((q) => q.vraieReponse);

    const payload: QuizCreateRequest = {
      contenu: JSON.stringify(contenuPayload),
      vraiesReponses: JSON.stringify(vraiesReponsesPayload),
      offerId: this.selectedOffer.id,
      methode: 0
    };

    const isUpdate = this.selectedQuizId() !== null;
    const request$ = isUpdate
      ? this.quizService.updateQuiz(payload)
      : this.quizService.createQuiz(payload);

    request$.subscribe({
      next: (quiz) => {
        this.selectedQuizId.set(quiz.id);
        this.success.set(isUpdate ? 'Quiz mis a jour avec succes !' : 'Quiz cree avec succes !');
        this.quizSubmitting.set(false);
        setTimeout(() => this.success.set(null), 3000);
      },
      error: (err: any) => {
        this.error.set('Erreur lors de l\'enregistrement du quiz');
        this.quizSubmitting.set(false);
        console.error(err);
      }
    });
  }

  isQuizFormValid(): boolean {
    if (this.quizQuestionsForm.length === 0) return false;

    const sanitizedQuestions = this.quizQuestionsForm.map((q) => ({
      question: q.question.trim(),
      choix: q.choix.map((c) => c.trim()).filter((c) => c.length > 0),
      vraieReponse: q.vraieReponse.trim()
    }));

    return sanitizedQuestions.every(
      (q) => q.question.length > 0 && q.choix.length >= 2 && q.vraieReponse.length > 0 && q.choix.includes(q.vraieReponse)
    );
  }

  private resolveTrueAnswer(rawAnswer: string | undefined, choices: string[]): string {
    const answer = (rawAnswer ?? '').trim();
    if (!answer) {
      return '';
    }

    const directMatch = choices.find((choice) => choice.trim().toLowerCase() === answer.toLowerCase());
    if (directMatch) {
      return directMatch;
    }

    const token = answer.charAt(0).toUpperCase();
    if (token >= 'A' && token <= 'D') {
      const idx = token.charCodeAt(0) - 'A'.charCodeAt(0);
      if (idx >= 0 && idx < choices.length) {
        return choices[idx];
      }
    }

    return '';
  }

  confirmDeleteQuiz(): void {
    const quizId = this.selectedQuizId();
    if (!quizId) return;
    if (!this.selectedOffer) return;

    this.quizSubmitting.set(true);
    this.error.set(null);

    this.quizService.deleteQuiz(quizId).subscribe({
      next: () => {
        this.resetQuizForm();
        this.loadQuizForOffer(this.selectedOffer!.id);
        this.success.set('Quiz supprimé avec succès !');
        this.quizSubmitting.set(false);
        setTimeout(() => this.success.set(null), 3000);
      },
      error: (err: any) => {
        this.error.set('Erreur lors de la suppression du quiz');
        this.quizSubmitting.set(false);
        console.error(err);
      }
    });
  }

  /**
   * Ouvre le modal pour voir les candidats qui ont passé le quiz
   */
  openInterviewsModal(offer: OfferResponse): void {
    this.selectedOffer = offer;
    this.passagesLoading.set(true);
    this.passages.set([]);
    this.showInterviewsModal.set(true);
    this.loadInterviews(offer.id);
  }

  /**
   * Charge les passages (quiz attempts) pour une offre
   */
  loadInterviews(offerId: number): void {
    this.passagesLoading.set(true);
    this.error.set(null);

    // First get the quiz for this offer
    this.quizService.getQuizByOffer(offerId).subscribe({
      next: (quiz) => {
        console.log('Quiz retrieved:', quiz);
        // Then get all passages for this quiz
        this.quizService.getQuizPassages(quiz.id).subscribe({
          next: (passages) => {
            console.log('Passages retrieved:', passages);
            this.passages.set(passages);
            this.passagesLoading.set(false);
          },
          error: (err) => {
            console.error('Erreur lors du chargement des passages:', err);
            console.error('Error details:', err.error);
            this.passages.set([]);
            this.passagesLoading.set(false);
            this.error.set('Erreur lors du chargement des passages: ' + (err.error?.message || err.message || 'Erreur inconnue'));
          }
        });
      },
      error: (err) => {
        console.error('Erreur lors du chargement du quiz:', err);
        console.error('Error details:', err.error);
        if (err.status === 404) {
          this.error.set('Aucun quiz n\'est associé à cette offre');
        } else {
          this.error.set('Erreur lors du chargement des interviews: ' + (err.error?.message || err.message || 'Erreur inconnue'));
        }
        this.passages.set([]);
        this.passagesLoading.set(false);
      }
    });
  }

  /**
   * Visualise le CV d'un candidat dans une nouvelle fenêtre
   */
  viewCandidateCv(passage: PassageWithCandidate): void {
    if (!this.selectedOffer) {
      this.error.set('Erreur: Offre non sélectionnée');
      return;
    }

    // Fetch the candidatures for this offer to get the CV path
    this.candidatureService.getCandidaturesByOffer(this.selectedOffer.id).subscribe({
      next: (candidatures: any[]) => {
        console.log('Candidatures retrieved:', candidatures);
        console.log('Looking for candidate email:', passage.candidatEmail);
        
        // Find the candidature for this specific candidate by email
        const candidature = candidatures.find(c => c.candidatEmail === passage.candidatEmail);
        
        console.log('Found candidature:', candidature);
        
        if (candidature && candidature.cvPath) {
          // Use the service method to construct the proper CV URL
          const cvUrl = this.candidatureService.getCvUrl(candidature.cvPath);
          console.log('Opening CV from URL:', cvUrl);
          window.open(cvUrl, '_blank');
        } else {
          console.log('No CV path found. Candidature:', candidature);
          this.error.set('CV non disponible pour ce candidat');
          setTimeout(() => this.error.set(null), 3000);
        }
      },
      error: (err) => {
        console.error('Error loading candidatures:', err);
        this.error.set('Erreur lors du chargement du CV');
        setTimeout(() => this.error.set(null), 3000);
      }
    });
  }

  /**
   * Invite un candidat à un entretien
   */
  inviteToInterview(passage: PassageWithCandidate): void {
    // This would typically open a dialog or send an email invite
    // For now, we'll show a success message
    this.success.set(`Invitation à un entretien envoyée à ${passage.candidatPrenom} ${passage.candidatNom} !`);
    setTimeout(() => this.success.set(null), 3000);
  }
}
