export interface Quiz {
  id: number;
  contenu: string;
  vraiesReponses: string;
}

export interface QuizCreateRequest {
  contenu: string;
  vraiesReponses: string;
  offerId: number;
}