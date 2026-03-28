import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-shell">
      <header class="dash-header">
        <span class="logo-mark">Recruiti</span>
        <nav class="nav-links">
          <button class="nav-btn" (click)="goToProfile()">My Profile</button>
          <button class="btn-outline" (click)="auth.logout()">Log out</button>
        </nav>
      </header>
      <main class="dash-main">
        <h1>Bienvenue, {{ auth.currentUser()?.prenom }} {{ auth.currentUser()?.nom }}!</h1>
        <p class="sub">Your dashboard is ready. More features coming soon.</p>
        <button class="btn-primary" (click)="goToProfile()">View My Profile</button>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-shell { 
      min-height: 100vh; 
      background: var(--bg); 
    }
    .dash-header { 
      display: flex; 
      justify-content: space-between; 
      align-items: center;
      padding: 1.25rem 2.5rem; 
      border-bottom: 1px solid var(--border); 
    }
    .logo-mark { 
      font-family: 'Playfair Display', serif; 
      font-size: 1.4rem; 
      color: var(--accent); 
      font-weight: 700; 
    }
    .nav-links {
      display: flex;
      gap: 1rem;
      align-items: center;
    }
    .nav-btn {
      background: none;
      border: none;
      color: var(--text);
      cursor: pointer;
      font-weight: 600;
      font-size: 0.95rem;
      padding: 0.5rem 1rem;
      border-radius: 0.25rem;
      transition: all 0.3s ease;
    }
    .nav-btn:hover {
      color: var(--accent);
      background: var(--bg-light);
    }
    .btn-outline {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text);
      padding: 0.5rem 1rem;
      border-radius: 0.5rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }
    .btn-outline:hover {
      border-color: var(--accent);
      color: var(--accent);
    }
    .btn-primary {
      background: var(--accent);
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 0.5rem;
      font-weight: 600;
      cursor: pointer;
      font-size: 0.95rem;
      transition: all 0.3s ease;
    }
    .btn-primary:hover {
      opacity: 0.9;
      transform: translateY(-2px);
    }
    .dash-main { 
      max-width: 960px; 
      margin: 6rem auto; 
      padding: 0 2rem; 
      text-align: center;
    }
    h1 { 
      font-family: 'Playfair Display', serif; 
      font-size: 2.5rem; 
      color: var(--text); 
    }
    .sub { 
      color: var(--text-muted); 
      margin-top: .5rem; 
      margin-bottom: 2rem;
    }
  `]
})
export class DashboardComponent {
  auth = inject(AuthService);
  private router = inject(Router);

  goToProfile() {
    this.router.navigate(['/profile']);
  }
}
