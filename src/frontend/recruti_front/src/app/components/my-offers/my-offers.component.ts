import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth';
import { OfferService,OfferResponse, OfferRequest  } from '../../services/offer.service';
import { CandidatureService } from '../../services/candidature.service';
import { PagedResponse } from '../../models/paged-response.model';

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

  // Candidatures
  candidatures = signal<any[]>([]);
  candidaturesLoading = signal<boolean>(false);

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
    private candidatureService: CandidatureService
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
    this.selectedOffer = null;
    this.resetForm();
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
}
