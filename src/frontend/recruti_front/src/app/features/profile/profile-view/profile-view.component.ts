import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { User } from '../../../core/models/user.model';
import { DeleteAccountModalComponent } from '../delete-account-modal/delete-account-modal.component';

@Component({
  selector: 'app-profile-view',
  standalone: true,
  imports: [CommonModule, DeleteAccountModalComponent],
  templateUrl: './profile-view.component.html',
  styleUrls: ['./profile-view.component.css']
})
export class ProfileViewComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  user = signal<User | null>(null);
  showDeleteModal = signal(false);

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.authService.getCurrentUserProfile().subscribe({
      next: (profile: User) => {
        this.user.set(profile);
      },
      error: (err: any) => {
        console.error('Error loading profile:', err);
      }
    });
  }

  formatRole(role?: string): string {
    if (!role) return 'Not provided';
    return role === 'RECRUTEUR' ? 'Recruiter' : role === 'CANDIDAT' ? 'Candidate' : role;
  }

  isProfessional(): boolean {
    return this.user()?.role === 'CANDIDAT' || this.user()?.role === 'RECRUTEUR';
  }

  calculateCompleteness(): number {
    const user = this.user();
    if (!user) return 0;

    let completed = 0;
    let total = 8;

    if (user.nom) completed++;
    if (user.prenom) completed++;
    if (user.email) completed++;
    if (user.dateNaissance) completed++;
    if (user.sexe) completed++;
    if (user.gouvernorat) completed++;
    if (user.poste) completed++;
    if (user.cvGenerique) completed++;

    return Math.round((completed / total) * 100);
  }

  goToEditProfile() {
    this.router.navigate(['/profile/edit']);
  }

  openDeleteModal() {
    this.showDeleteModal.set(true);
  }

  closeDeleteModal() {
    this.showDeleteModal.set(false);
  }

  handleDeleteAccount() {
    this.showDeleteModal.set(false);
  }

  downloadCV() {
    const user = this.user();
    if (user?.cvGenerique) {
      const link = document.createElement('a');
      link.href = 'data:application/octet-stream;base64,' + user.cvGenerique;
      link.download = `CV_${user.nom}_${user.prenom}.pdf`;
      link.click();
    }
  }
}
