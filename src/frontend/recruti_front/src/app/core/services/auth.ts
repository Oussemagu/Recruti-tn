import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { User, LoginRequest, RegisterRequest, UpdateUserRequest, VerifyPasswordRequest } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly API = `${environment.apiUrl}/api/auth`;
  private readonly USERS_API = `${environment.apiUrl}/api/users`;

  // Signal contenant l'utilisateur connecté (null si non connecté)
  private _currentUser = signal<User | null>(null);

  readonly currentUser  = this._currentUser.asReadonly();
   readonly userRole = computed(() => this._currentUser()?.role ?? null);
  readonly userId = computed(() => this._currentUser()?.id ?? null);

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
      tap((user: User) => this._persist(user))
    );
  }

  /**
   * Appel POST /api/auth/login
   * Envoie email + password au backend et reçoit le token JWT
   */
  login(dto: LoginRequest): Observable<User> {
    return this.http.post<User>(`${this.API}/login`, dto).pipe(
      tap((user: User) => this._persist(user))
    );
  }

  /**
   * Récupère le profil complet de l'utilisateur actuellement connecté
   * GET /api/users/profile
   */
  getCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.USERS_API}/profile`).pipe(
      tap((user: User) => this._updateCurrentUser(user))
    );
  }

  /**
   * Met à jour le profil de l'utilisateur actuellement connecté
   * PUT /api/users/profile
   */
  updateCurrentUserProfile(updateRequest: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.USERS_API}/profile`, updateRequest).pipe(
      tap((user: User) => this._updateCurrentUser(user))
    );
  }

  /**
   * Supprime le compte de l'utilisateur actuellement connecté
   * DELETE /api/users/profile
   */
  deleteCurrentUserAccount(): Observable<void> {
    return this.http.delete<void>(`${this.USERS_API}/profile`).pipe(
      tap(() => this.logout())
    );
  }

  /**
   * Vérifie le mot de passe de l'utilisateur actuel avant suppression
   * POST /api/users/profile/verify-password
   */
  verifyCurrentUserPassword(password: string): Observable<{ valid: boolean }> {
    return this.http.post<{ valid: boolean }>(
      `${this.USERS_API}/profile/verify-password`,
      { password } as VerifyPasswordRequest
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

  // Met à jour l'utilisateur actuel sans modifier le token
  private _updateCurrentUser(user: User): void {
    const current = this._currentUser();
    if (current && user.id === current.id) {
      const updatedUser = {
        ...current,
        ...user,
        token: current.token // Préserve le token existant
      };
      this._currentUser.set(updatedUser);
      localStorage.setItem('recruiti_user', JSON.stringify(updatedUser));
    }
  }


  // Dans AuthService — méthodes à ajouter

private readonly USER_KEY = 'recruiti_user';

getCurrentUser(): any {
  const raw = localStorage.getItem(this.USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

isRecruteur(): boolean {
  return this.getCurrentUser()?.role === 'RECRUTEUR';
}

isAuthenticated(): boolean {
  return !!this.getCurrentUser()?.token;
}
}
