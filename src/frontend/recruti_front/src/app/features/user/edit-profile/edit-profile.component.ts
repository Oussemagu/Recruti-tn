import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../models/user.model';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
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
  newSkill: string = '';
  resumeFile: File | null = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.skillsList = this.user.skills.split(',').map(s => s.trim()).filter(s => s);
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
    // Update user skills to the joined string
    this.user.skills = this.skillsList.join(', ');
    
    console.log('Saving user:', this.user);
    
    // Navigate back to profile
    this.router.navigate(['/profile']);
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }
}
