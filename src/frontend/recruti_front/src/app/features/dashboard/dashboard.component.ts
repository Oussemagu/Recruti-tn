import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-shell">
      <header class="dash-header">
        <span class="logo-mark">✦ Recruiti</span>
        <button class="btn-outline" (click)="auth.logout()">Log out</button>
      </header>
      <main class="dash-main">
        <h1>Bienvenue, {{ auth.currentUser()?.prenom }} {{ auth.currentUser()?.nom }} !</h1>
        <p class="sub">Your dashboard is ready. More features coming soon.</p>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-shell { min-height: 100vh; background: var(--bg); }
    .dash-header { display: flex; justify-content: space-between; align-items: center;
      padding: 1.25rem 2.5rem; border-bottom: 1px solid var(--border); }
    .logo-mark { font-family: 'Playfair Display', serif; font-size: 1.4rem; color: var(--accent); font-weight: 700; }
    .dash-main { max-width: 960px; margin: 6rem auto; padding: 0 2rem; }
    h1 { font-family: 'Playfair Display', serif; font-size: 2.5rem; color: var(--text); }
    .sub { color: var(--text-muted); margin-top: .5rem; }
  `]
})
export class DashboardComponent {
  auth = inject(AuthService);
}
