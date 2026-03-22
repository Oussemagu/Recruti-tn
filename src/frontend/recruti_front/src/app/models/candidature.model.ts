// src/app/models/candidature.model.ts
export interface Candidature {
  idCandidature: number;
  idOffre: number;
  cvPath: string;
  datePostulation: string;
  scoreCv: number;
  status: string;
}
