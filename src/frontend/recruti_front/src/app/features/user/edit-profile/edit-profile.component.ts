import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../models/user.model';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  user!: User;
  skillsList: string[] = [];
  newSkill: string = '';
  resumeFile: File | null = null;

  constructor(private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    const userId = 1; // Testing with user ID 1
    this.userService.getUser(userId).subscribe({
      next: (data) => {
        this.user = data;
        this.skillsList = data.skills ? data.skills.split(',').map(s => s.trim()).filter(s => s) : [];
      },
      error: (err) => console.error('Error fetching user:', err)
    });
  }

  addSkill(): void {
    if (this.newSkill.trim()) {
      this.skillsList.push(this.newSkill.trim());
      this.newSkill = '';
    }
  }

  removeSkill(index: number): void {
    this.skillsList.splice(index, 1);
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.resumeFile = file;
    }
  }

  saveChanges(): void {
    this.user.skills = this.skillsList.join(', ');
    
    this.userService.updateUser(this.user.id, this.user).subscribe({
      next: () => {
        alert('Profile updated successfully!');
        this.router.navigate(['/profile']);
      },
      error: (err) => console.error('Update failed', err)
    });
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }
}
