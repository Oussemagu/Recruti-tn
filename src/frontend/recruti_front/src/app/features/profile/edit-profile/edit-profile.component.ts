import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { UpdateUserRequest, User } from '../../../core/models/user.model';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  profileForm!: FormGroup;
  isSubmitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  ngOnInit() {
    this.initializeForm();
    this.loadUserData();
  }

  initializeForm() {
    this.profileForm = this.fb.group({
      nom: [''],
      prenom: [''],
      email: ['', [Validators.email]],
      dateNaissance: [''],
      sexe: [''],
      gouvernorat: [''],
      poste: [''],
      nomSociete: [''],
      skills: [''],
      password: ['']
    });
  }

  loadUserData() {
    this.authService.getCurrentUserProfile().subscribe({
      next: (user: User) => {
        this.profileForm.patchValue({
          nom: user.nom || '',
          prenom: user.prenom || '',
          email: user.email || '',
          dateNaissance: user.dateNaissance || '',
          sexe: user.sexe || '',
          gouvernorat: user.gouvernorat || '',
          poste: user.poste || '',
          nomSociete: user.nomSociete || '',
          skills: user.skills || ''
        });
        this.profileForm.markAsPristine();
      },
      error: (err: any) => {
        this.errorMessage.set('Failed to load profile data');
        console.error('Error loading profile:', err);
      }
    });
  }

  saveChanges() {
    if (this.profileForm.invalid) {
      this.errorMessage.set('Please fix the errors in the form');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const updateRequest: UpdateUserRequest = {};
    const formValue = this.profileForm.value;

    // Only include changed fields
    Object.keys(formValue).forEach((key) => {
      if (formValue[key]) {
        (updateRequest as any)[key] = formValue[key];
      }
    });

    this.authService.updateCurrentUserProfile(updateRequest).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.successMessage.set('Profile updated successfully');
        this.profileForm.markAsPristine();
        setTimeout(() => {
          this.router.navigate(['/profile']);
        }, 1500);
      },
      error: (err: any) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to update profile');
        console.error('Error updating profile:', err);
      }
    });
  }
}
