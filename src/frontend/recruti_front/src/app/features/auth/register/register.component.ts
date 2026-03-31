import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { RegisterRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  step = 1;

  private auth = inject(AuthService);
  private router = inject(Router);

  form: RegisterRequest = {
    nom: '',
    prenom: '',
    email: '',
    password: '',
    role: null,
    dateNaissance: '',
    nomSociete: '',
    poste: '',
    skills: '',
    gouvernorat: '',
    sexe: '',
  };

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

  nextStep() { if (this.step < 3) this.step++; }
  prevStep() { if (this.step > 1) this.step--; }

  /**
   * Called when role changes in step 2.
   * Clears fields that don't belong to the selected role
   * so no ghost data is sent to the backend.
   */
  onRoleChange() {
    if (this.form.role === 'CANDIDAT') {
        this.form.nomSociete = '';
        this.form.poste = '';
    } else if (this.form.role === 'RECRUTEUR') {
        this.form.skills = '';
    }
}

 onSubmit() {
    // Guard — should never happen since the button is disabled, but safety net
    if (!this.form.role) {
      this.error = 'Please select a role.';
      return;
    }

    // Cleanup ghost fields based on role
    if (this.form.role === 'CANDIDAT') {
      this.form.nomSociete = '';
      this.form.poste = '';
    } else if (this.form.role === 'RECRUTEUR') {
      this.form.skills = '';
    }

    this.loading = true;
    this.error = '';

    this.auth.register(this.form).subscribe({
      next: () => {
        this.router.navigate(['/candidat']);
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Inscription échouée. Réessayez.';
        this.loading = false;
      }
    });
}
}
