import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { OfferService } from '../../services/offer.service';
import { Offer } from '../../models/offer.model';

@Component({
  selector: 'app-recruiter-offers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './recruiter-offers.component.html',
  styleUrl: './recruiter-offers.component.css'
})
export class RecruiterOffersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  offers: Offer[] = [];
  isLoading = false;
  errorMessage = '';

  searchTitre = '';
  filterAvailableOnly = false;

  showFormModal = false;
  isEditMode = false;
  selectedOfferId: number | null = null;

  readonly offerForm = this.fb.group({
    titre: ['', [Validators.required]],
    description: ['', [Validators.required]],
    tags: [''],
    available: [true],
    dateEmission: ['', [Validators.required]]
  });

  constructor(private readonly offerService: OfferService) {}

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.offerService.getAllOffers().subscribe({
      next: (data) => {
        this.offers = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement des offres.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    this.isLoading = true;
    this.errorMessage = '';

    if (this.searchTitre.trim()) {
      this.offerService.searchByTitre(this.searchTitre.trim()).subscribe({
        next: (data) => {
          this.offers = this.filterAvailableOnly ? data.filter((o) => o.available) : data;
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors de la recherche par titre.';
          this.isLoading = false;
        }
      });
      return;
    }

    if (this.filterAvailableOnly) {
      this.offerService.filterByAvailable(true).subscribe({
        next: (data) => {
          this.offers = data;
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors du filtrage par disponibilité.';
          this.isLoading = false;
        }
      });
      return;
    }

    this.loadOffers();
  }

  resetFilters(): void {
    this.searchTitre = '';
    this.filterAvailableOnly = false;
    this.loadOffers();
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedOfferId = null;
    this.offerForm.reset({
      titre: '',
      description: '',
      tags: '',
      available: true,
      dateEmission: ''
    });
    this.showFormModal = true;
  }

  openEditModal(offer: Offer): void {
    this.isEditMode = true;
    this.selectedOfferId = offer.id ?? null;
    this.offerForm.patchValue({
      titre: offer.titre,
      description: offer.description,
      tags: offer.tags,
      available: offer.available,
      dateEmission: offer.dateEmission
    });
    this.showFormModal = true;
  }

  closeModal(): void {
    this.showFormModal = false;
  }

  saveOffer(): void {
    if (this.offerForm.invalid) {
      this.offerForm.markAllAsTouched();
      return;
    }

    const formValue = this.offerForm.getRawValue();
    const payload: Offer = {
      titre: formValue.titre ?? '',
      description: formValue.description ?? '',
      tags: formValue.tags ?? '',
      available: formValue.available ?? false,
      dateEmission: formValue.dateEmission ?? ''
    };

    this.isLoading = true;
    this.errorMessage = '';

    if (this.isEditMode && this.selectedOfferId !== null) {
      this.offerService.updateOffer(this.selectedOfferId, payload).subscribe({
        next: () => {
          this.showFormModal = false;
          this.applyFilters();
        },
        error: () => {
          this.errorMessage = 'Erreur lors de la modification de l\'offre.';
          this.isLoading = false;
        }
      });
      return;
    }

    this.offerService.createOffer(payload).subscribe({
      next: () => {
        this.showFormModal = false;
        this.applyFilters();
      },
      error: () => {
        this.errorMessage = 'Erreur lors de la création de l\'offre.';
        this.isLoading = false;
      }
    });
  }

  confirmDelete(offer: Offer): void {
    if (!offer.id) {
      return;
    }

    const isConfirmed = window.confirm(`Supprimer l'offre "${offer.titre}" ?`);
    if (!isConfirmed) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.offerService.deleteOffer(offer.id).subscribe({
      next: () => this.applyFilters(),
      error: () => {
        this.errorMessage = 'Erreur lors de la suppression de l\'offre.';
        this.isLoading = false;
      }
    });
  }
}