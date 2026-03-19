import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-delete-account',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delete-account.component.html',
  styleUrls: ['./delete-account.component.css']
})
export class DeleteAccountComponent {
  password: string = '';
  error: string = '';
  isLoading: boolean = false;
  showConfirmation: boolean = false;

  // Mock user password (in real app, this would be verified via backend)
  correctPassword = 'password123';

  constructor(private router: Router) {}

  verifyPassword(): void {
    this.error = '';

    if (!this.password) {
      this.error = 'Please enter your password';
      return;
    }

    this.isLoading = true;

    // Simulate API call
    setTimeout(() => {
      if (this.password === this.correctPassword) {
        this.showConfirmation = true;
      } else {
        this.error = 'Incorrect password. Please try again.';
      }
      this.isLoading = false;
    }, 500);
  }

  confirmDelete(): void {
    this.isLoading = true;

    // Simulate account deletion
    setTimeout(() => {
      console.log('Account deleted successfully');
      // In real app, you would call a delete API endpoint here
      // Then redirect to login or home page
      this.router.navigate(['/']);
    }, 1000);
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }
}
