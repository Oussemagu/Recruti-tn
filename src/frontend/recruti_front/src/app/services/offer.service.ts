import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Offer } from '../models/offer.model';

@Injectable({
  providedIn: 'root'
})
export class OfferService {
  private readonly baseUrl = `${environment.apiUrl}/api/offers`;

  constructor(private readonly http: HttpClient) {}

  getAllOffers(): Observable<Offer[]> {
    return this.http.get<Offer[]>(this.baseUrl);
  }

  getOfferById(id: number): Observable<Offer> {
    return this.http.get<Offer>(`${this.baseUrl}/${id}`);
  }

  createOffer(offer: Offer): Observable<Offer> {
    return this.http.post<Offer>(this.baseUrl, offer);
  }

  updateOffer(id: number, offer: Offer): Observable<Offer> {
    return this.http.put<Offer>(`${this.baseUrl}/${id}`, offer);
  }

  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  searchByTitre(titre: string): Observable<Offer[]> {
    return this.http.get<Offer[]>(`${this.baseUrl}/titre/${encodeURIComponent(titre)}`);
  }

  filterByAvailable(available: boolean): Observable<Offer[]> {
    return this.http.get<Offer[]>(`${this.baseUrl}/available/${available}`);
  }

  filterByDate(date: string): Observable<Offer[]> {
    return this.http.get<Offer[]>(`${this.baseUrl}/date/${date}`);
  }

  filterByDateRange(d1: string, d2: string): Observable<Offer[]> {
    return this.http.get<Offer[]>(`${this.baseUrl}/date-between/${d1}/${d2}`);
  }
}