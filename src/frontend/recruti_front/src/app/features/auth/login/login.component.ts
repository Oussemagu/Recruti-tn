import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { LoginRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit {
  role: string = 'recruiter';  // a verifier
  private auth   = inject(AuthService);
  private route  = inject(ActivatedRoute);
  private router = inject(Router);

  form: LoginRequest = { email: '', password: '' };
  showPw  = false;
  loading = false;
  error   = '';

  ngOnInit() {

  }

  onSubmit() {
    this.loading = true;
    this.error   = '';

    this.auth.login(this.form).subscribe({
      next: () => {
        // Redirection après connexion réussie
        this.router.navigate(['/candidat/offres']);
      },
      error: (err:any) => {
        // 401 → mauvais identifiants
        this.error = err.status === 401
          ? 'Email ou mot de passe incorrect.'
          : 'Connexion échouée. Réessayez.';
        this.loading = false;
      }
    });
  }
}
