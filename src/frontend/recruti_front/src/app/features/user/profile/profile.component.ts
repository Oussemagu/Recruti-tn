import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { User } from '../models/user.model';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user!: User;
  skillsList: string[] = [];
  profileCompleteness: number = 85;

  constructor(private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    const userId = 3; // Testing with user ID 1
    this.userService.getUser(userId).subscribe({
      next: (data) => {
        this.user = data;
        this.skillsList = data.skills ? data.skills.split(',').map(s => s.trim()).filter(s => s) : [];
      },
      error: (err) => console.error('Error fetching user:', err)
    });
  }

  editProfile(): void {
    this.router.navigate(['/edit-profile']);
  }

  deleteAccount(): void {
    this.router.navigate(['/delete-account']);
  }

  downloadResume(): void {
    console.log('Download resume:', this.user.cvGenerique);
    // Implement download logic
  }
}
