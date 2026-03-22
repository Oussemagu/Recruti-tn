import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="mx-auto mt-10 max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow">
      <h2 class="mb-1 text-2xl font-semibold text-slate-900">
        {{ mode() === 'register' ? 'Créer un compte' : 'Connexion' }}
      </h2>
      <p class="mb-6 text-sm text-slate-600">
        Interface de test pour accéder aux espaces candidat et recruteur.
      </p>

      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Email</label>
          <input
            type="email"
            formControlName="email"
            class="w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none"
            placeholder="utilisateur@recruti.tn"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Mot de passe</label>
          <input
            type="password"
            formControlName="password"
            class="w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none"
            placeholder="********"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Rôle</label>
          <select
            formControlName="role"
            class="w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none"
          >
            <option value="candidate">Candidat</option>
            <option value="recruiter">Recruteur</option>
          </select>
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">ID utilisateur</label>
          <input
            type="number"
            formControlName="userId"
            class="w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none"
            min="1"
          />
        </div>

        <button
          type="submit"
          [disabled]="form.invalid"
          class="w-full rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {{ mode() === 'register' ? 'Créer le compte' : 'Se connecter' }}
        </button>
      </form>

      <p class="mt-4 text-center text-sm text-slate-600">
        <a
          *ngIf="mode() !== 'register'"
          routerLink="/auth/register"
          class="text-indigo-600 hover:underline"
        >
          Créer un compte
        </a>
        <a
          *ngIf="mode() === 'register'"
          routerLink="/auth/login"
          class="text-indigo-600 hover:underline"
        >
          Déjà inscrit ? Se connecter
        </a>
      </p>
    </section>
  `
})
export class AuthPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly mode = computed(() => (this.route.snapshot.data['mode'] as 'register' | undefined) ?? 'login');

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: this.fb.nonNullable.control<'candidate' | 'recruiter'>('candidate'),
    userId: this.fb.nonNullable.control(1, [Validators.required, Validators.min(1)])
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const role = this.form.controls.role.value;
    const userId = this.form.controls.userId.value;

    localStorage.setItem('userRole', role);
    localStorage.setItem('userId', String(userId));

    if (role === 'recruiter') {
      this.router.navigate(['/offers/recruiter']);
      return;
    }

    this.router.navigate(['/offers/candidate']);
  }
}
