import { Component, inject, signal, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-delete-account-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delete-account-modal.component.html',
  styleUrls: ['./delete-account-modal.component.css']
})
export class DeleteAccountModalComponent {
  private authService = inject(AuthService);

  password = '';
  isLoading = signal(false);
  errorMessage = signal('');

  onCancel = output<void>();
  onConfirm = output<void>();

  cancel() {
    this.onCancel.emit();
  }

  verify() {
    if (!this.password) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.verifyCurrentUserPassword(this.password).subscribe({
      next: (response: any) => {
        if (response.valid) {
          this.deleteAccount();
        } else {
          this.isLoading.set(false);
          this.errorMessage.set('Incorrect password. Please try again.');
        }
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to verify password. Please try again.');
        console.error('Error verifying password:', err);
      }
    });
  }

  private deleteAccount() {
    this.authService.deleteCurrentUserAccount().subscribe({
      next: () => {
        this.onConfirm.emit();
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to delete account. Please try again.');
        console.error('Error deleting account:', err);
      }
    });
  }
}
