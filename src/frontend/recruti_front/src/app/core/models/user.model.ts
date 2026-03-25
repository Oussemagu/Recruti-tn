export type UserRole = 'RECRUTEUR' | 'CANDIDAT' | 'ADMIN';

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  role: UserRole;
  dateNaissance: string;
  sexe?: string;
  gouvernorat?: string;
  poste?: string;
  nomSociete?: string;
  skills?: string;
}


export interface User {
  token: string;
  email: string;
  nom: string;
  prenom: string;
  role: UserRole;
}


export interface LoginRequest {
  email: string;
  password: string;
}