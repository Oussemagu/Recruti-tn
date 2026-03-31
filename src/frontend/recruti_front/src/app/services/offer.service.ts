import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Offer } from '../models/offer.model';
import { PagedResponse } from '../models/paged-response.model';

export interface OfferRequest {
  titre: string;
  description: string;
  tags: string;
  available: boolean;
  idRecruteur: number;
  dateEmission: string;
}

export interface OfferResponse {
  id: number;
  dateEmission: string;
  titre: string;
  description: string;
  tags: string;
  available: boolean;
  idRecruteur: number;
}

@Injectable({
  providedIn: 'root'
})
export class OfferService {
  private readonly baseUrl = `${environment.apiUrl}/api/offers`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Récupère les offres disponibles (paginé avec filtre)
   */
  getAvailableOffers(page = 1, limit = 6, titre = ''): Observable<PagedResponse<Offer>> {
    const params = new HttpParams()
      .set('page', page)
      .set('limit', limit)
      .set('titre', titre);
    return this.http.get<PagedResponse<Offer>>(`${this.baseUrl}/available`, { params });
  }

  /**
   * Récupère toutes les offres
   */
  getAllOffers(): Observable<Offer[]> {
    return this.http.get<Offer[]>(this.baseUrl);
  }

  /**
   * Récupère les offres (paginé)
   */
  getOffers(page = 1, limit = 6): Observable<PagedResponse<Offer>> {
    const params = new HttpParams()
      .set('page', page)
      .set('limit', limit);
    return this.http.get<PagedResponse<Offer>>(`${this.baseUrl}`, { params });
  }

  /**
   * Récupère toutes les offres d'un recruteur spécifique (paginé)
   * GET /api/offers/recruiter/{recruiterId}?page=1&limit=6
   */
  getMyOffers(recruiterId: number, page: number = 1, limit: number = 6): Observable<PagedResponse<OfferResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('limit', limit);
    return this.http.get<PagedResponse<OfferResponse>>(
      `${this.baseUrl}/recruiter/${recruiterId}`,
      { params }
    );
  }

  /**
   * Crée une nouvelle offre
   * POST /api/offers
   */
  addOffer(offer: OfferRequest): Observable<Offer> {
    return this.http.post<Offer>(this.baseUrl, offer);
  }

  /**
   * Met à jour une offre existante
   * PUT /api/offers/{id}
   */
  updateOffer(id: number, offer: OfferRequest): Observable<Offer> {
    return this.http.put<Offer>(`${this.baseUrl}/${id}`, offer);
  }

  /**
   * Supprime une offre
   * DELETE /api/offers/{id}
   */
  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
