import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DeleteAccountService } from './delete-account.service';

@Component({
  selector: 'app-verify-delete-account',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verify-delete-account.component.html',
  styleUrls: ['./verify-delete-account.component.css']
})
export class VerifyDeleteAccountComponent {
  password: string = '';
  error: string = '';
  isLoading: boolean = false;

  // Mock user password (in real app, verify via backend)
  correctPassword = 'password123';

  constructor(private router: Router, private deleteService: DeleteAccountService) {}

  verifyPassword(): void {
    this.error = '';

    if (!this.password) {
      this.error = 'Please enter your password';
      return;
    }

    this.isLoading = true;

    setTimeout(() => {
      if (this.password === this.correctPassword) {
        this.deleteService.setVerified(true);
        this.router.navigate(['/delete-account/confirm']);
      } else {
        this.error = 'Incorrect password. Please try again.';
      }
      this.isLoading = false;
    }, 500);
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }
}
