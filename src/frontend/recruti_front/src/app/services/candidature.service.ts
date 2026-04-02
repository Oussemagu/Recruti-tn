// candidature.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Candidature } from '../models/candidature.model';

@Injectable({
  providedIn: 'root'
})
export class CandidatureService {
  private readonly baseUrl = `${environment.apiUrl}/api/candidatures`;

  constructor(private readonly http: HttpClient) {}

  postuler(candidatId: number, offreId: number, cv: File): Observable<string> {
    const formData = new FormData();
    formData.append('candidatId', String(candidatId));
    formData.append('offreId', String(offreId));
    formData.append('cv', cv, cv.name);
    return this.http.post(this.baseUrl, formData, { responseType: 'text' });
  }

  getCandidaturesByCandidat(candidatId: number): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.baseUrl}/candidat/${candidatId}`);
  }

  getCandidatureById(idCandidature: number): Observable<Candidature> {
    return this.http.get<Candidature>(`${this.baseUrl}/${idCandidature}`);
  }

  deleteCandidature(idCandidature: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${idCandidature}`, { responseType: 'text' });
  }

  getCandidaturesByOffer(offerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/offre/${offerId}`);
  }

  // Méthode pour obtenir l'URL du CV (le backend servira le fichier)
  getCvUrl(cvPath: string): string {
    // Le cvPath du backend ressemble à: /app/uploads/cvs/cv_1_1773935872198.pdf
    // On extrait juste le nom du fichier
    const fileName = cvPath.split('/').pop();
    return `${environment.apiUrl}/api/candidatures/cv/${fileName}`;
  }
}
