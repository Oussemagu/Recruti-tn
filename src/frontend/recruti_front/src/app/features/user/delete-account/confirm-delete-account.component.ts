import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DeleteAccountService } from './delete-account.service';

@Component({
  selector: 'app-confirm-delete-account',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-delete-account.component.html',
  styleUrls: ['./confirm-delete-account.component.css']
})
export class ConfirmDeleteAccountComponent implements OnInit {
  isLoading = false;

  constructor(private router: Router, private deleteService: DeleteAccountService) {}

  ngOnInit(): void {
    if (!this.deleteService.isVerified()) {
      this.router.navigate(['/delete-account']);
    }
  }

  confirmDelete(): void {
    this.isLoading = true;

    setTimeout(() => {
      console.log('Account deleted successfully');
      this.deleteService.clear();
      this.router.navigate(['/']);
    }, 1000);
  }

  cancel(): void {
    this.deleteService.clear();
    this.router.navigate(['/profile']);
  }
}
