import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { User, LoginRequest, RegisterRequest } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly API = `${environment.apiUrl}/api/auth`;

  // Signal contenant l'utilisateur connecté (null si non connecté)
  private _currentUser = signal<User | null>(null);

  readonly currentUser  = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => !!this._currentUser());
  readonly userRole = computed(() => this._currentUser()?.role ?? null);

  constructor(private http: HttpClient, private router: Router) {
    // Restaure la session depuis localStorage au démarrage
    const saved = localStorage.getItem('recruiti_user');
    if (saved) {
      this._currentUser.set(JSON.parse(saved));
    }
  }

  /**
   * Appel POST /api/auth/register
   * Envoie les données d'inscription au backend Spring Boot
   */
  register(dto: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.API}/register`, dto).pipe(
      tap(user => this._persist(user))
    );
  }

  /**
   * Appel POST /api/auth/login
   * Envoie email + password au backend et reçoit le token JWT
   */
  login(dto: LoginRequest): Observable<User> {
    return this.http.post<User>(`${this.API}/login`, dto).pipe(
      tap(user => this._persist(user))
    );
  }

  logout(): void {
    this._currentUser.set(null);
    localStorage.removeItem('recruiti_user');
    this.router.navigate(['/']);
  }

  // Sauvegarde l'utilisateur + token dans localStorage et dans le signal
  private _persist(user: User): void {
    this._currentUser.set(user);
    localStorage.setItem('recruiti_user', JSON.stringify(user));
  }
}