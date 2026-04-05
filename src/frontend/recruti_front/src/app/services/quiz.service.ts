import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Quiz, QuizCreateRequest } from '../models/quiz.model';

@Injectable({
  providedIn: 'root'
})
export class QuizService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  createQuiz(payload: QuizCreateRequest): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.baseUrl}/createQuiz`, payload);
  }

  getQuizByOffer(offerId: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.baseUrl}/getQuiz/${offerId}`);
  }

  updateQuiz(payload: QuizCreateRequest): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.baseUrl}/updateQuiz`, payload);
  }

  deleteQuiz(quizId: number): Observable<Quiz> {
    return this.http.delete<Quiz>(`${this.baseUrl}/deleteQuiz/${quizId}`);
  }
}