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

export interface QuizSubmissionRequest {
  quizId: number;
  candidatId: number;
  answers: string[];
}

export interface QuizResult {
  passageId: number;
  score: number;
  totalQuestions: number;
  datePassage: string;
}