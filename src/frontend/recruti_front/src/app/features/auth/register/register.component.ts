import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { RegisterRequest } from '../../../core/models/user.model';
import {Router} from "@angular/router";
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  step = 1;

  nextStep() { if (this.step < 3) this.step++; }
  prevStep() { if (this.step > 1) this.step--; }
  private auth = inject(AuthService);
  private router = inject(Router);

 form: RegisterRequest = {
    nom: '',
    prenom: '',
    email: '',
    password: '',
    role: 'RECRUTEUR',
    dateNaissance: '',
    nomSociete: '',
    poste: '',
    skills: '',
    gouvernorat: '',
    sexe: '',
  };

  industries = ['Technology', 'Finance', 'Healthcare', 'Education', 'Retail', 'Manufacturing', 'Other'];
  sizes = ['1-10', '11-50', '51-200', '201-500', '500+'];
  gouvernorats = [
  'Ariana', 'Béja', 'Ben Arous', 'Bizerte', 'Gabès',
  'Gafsa', 'Jendouba', 'Kairouan', 'Kasserine', 'Kébili',
  'Le Kef', 'Mahdia', 'Manouba', 'Médenine', 'Monastir',
  'Nabeul', 'Sfax', 'Sidi Bouzid', 'Siliana', 'Sousse',
  'Tataouine', 'Tozeur', 'Tunis', 'Zaghouan'
];
  showPw = false;
  loading = false;
  error = '';

  onSubmit() {
    this.loading = true;
    this.error   = '';

    this.auth.register(this.form).subscribe({
      next: () => {
        // Redirection après inscription réussie
        this.router.navigate(['/dashboard']);
      },
      error: (err:any) => {
        // Affiche le message d'erreur du backend si disponible
        this.error = err.error?.message || 'Inscription échouée. Réessayez.';
        this.loading = false;
      }
    });
  }
}

