import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface RoleStats {
  CANDIDAT: number;
  RECRUTEUR: number;
  ADMIN: number;
}

interface SkillStats {
  [skill: string]: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiUrl}/api/dashboard`;

  getRoleStatistics(): Observable<RoleStats> {
    return this.http.get<RoleStats>(`${this.API}/role-stats`);
  }

  getSkillStatistics(): Observable<SkillStats> {
    return this.http.get<SkillStats>(`${this.API}/skill-stats`);
  }
}
