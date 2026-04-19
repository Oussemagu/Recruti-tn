import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { UpdateUserRequest, User } from '../../core/models/user.model';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-candidate-offers',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  animations: [
    trigger('dropdownAnim', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-8px)' }),
        animate('180ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('140ms ease-in', style({ opacity: 0, transform: 'translateY(-8px)' }))
      ])
    ]),
    trigger('modalAnim', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.95)' }),
        animate('200ms ease-out', style({ opacity: 1, transform: 'scale(1)' }))
      ]),
      transition(':leave', [
        animate('150ms ease-in', style({ opacity: 0, transform: 'scale(0.95)' }))
      ])
    ])
  ],
  templateUrl: './candidate-offers.component.html',
  styleUrl: './candidate-offers.component.css'
})
export class CandidateOffersComponent implements OnInit {  // ← implémenter OnInit
  auth = inject(AuthService);
  private router = inject(Router);
   showProfileDropdown = false;
  showEditModal = signal(false);
  isSubmitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  // Champs du formulaire édition
  editForm: UpdateUserRequest = {};

  ngOnInit() {
    // ← charger le profil dès le départ
    this.auth.getCurrentUserProfile().subscribe();
  }

  isRecruteur(): boolean {
    return this.auth.isRecruteur();
  }
  isAdmin(): boolean {
    return this.auth.isAdmin();
  }
  toggleProfileDropdown(): void {
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  closeProfileDropdown(): void {
    this.showProfileDropdown = false;
  }

  // ← ouvre le modal au lieu de naviguer
  openEditModal(): void {
    const user = this.auth.currentUser();
    this.editForm = {
      nom:          user?.nom || '',
      prenom:       user?.prenom || '',
      email:        user?.email || '',
      dateNaissance: user?.dateNaissance || '',
      sexe:         user?.sexe || '',
      gouvernorat:  user?.gouvernorat || '',
      poste:        user?.poste || '',
      nomSociete:   user?.nomSociete || '',
      skills:       user?.skills || '',
    };
    this.errorMessage.set('');
    this.successMessage.set('');
    this.showEditModal.set(true);
    this.closeProfileDropdown();
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
  }

  saveProfile(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    // Filtrer les champs vides
    const payload: UpdateUserRequest = {};
    Object.keys(this.editForm).forEach(key => {
      const val = (this.editForm as any)[key];
      if (val !== '' && val !== null && val !== undefined) {
        (payload as any)[key] = val;
      }
    });

    this.auth.updateCurrentUserProfile(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.successMessage.set('Profil mis à jour avec succès !');
        setTimeout(() => this.closeEditModal(), 1500);
      },
      error: (err: any) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message || 'Erreur lors de la mise à jour');
      }
    });
  }
}
