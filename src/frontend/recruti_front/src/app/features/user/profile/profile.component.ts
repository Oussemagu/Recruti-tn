import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { User } from '../models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: User = {
    id: 1,
    nom: 'Johnson',
    prenom: 'Sarah',
    email: 'sarah.j@email.com',
    role: 'Candidate',
    dateNaissance: '1995-05-15',
    skills: 'React, TypeScript, Node.js, Python, Angular',
    sexe: 'Female',
    gouvernorat: 'Tunis',
    poste: 'Software Developer',
    nomSociete: 'Tech Company Inc',
    cvGenerique: 'resume.pdf'
  };

  skillsList: string[] = [];
  profileCompleteness: number = 85;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Parse skills from string
    this.skillsList = this.user.skills.split(',').map(s => s.trim()).filter(s => s);
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
