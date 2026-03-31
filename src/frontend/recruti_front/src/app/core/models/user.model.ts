export type UserRole = 'RECRUTEUR' | 'CANDIDAT' | 'ADMIN' ;

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  role: UserRole | null;
  dateNaissance: string;
  sexe?: string;
  gouvernorat?: string;
  poste?: string;
  nomSociete?: string;
  skills?: string;
}


export interface User {
   id: number;
  token: string;
  email: string;
  nom: string;
  prenom: string;
  role: UserRole;
  dateNaissance?: string;
  sexe?: string;
  gouvernorat?: string;
  poste?: string;
  nomSociete?: string;
  skills?: string;
  cvGenerique?: string;
}


export interface LoginRequest {
  email: string;
  password: string;
}
export interface VerifyPasswordRequest {
  password: string;
}
export interface UpdateUserRequest {
  nom?: string;
  prenom?: string;
  email?: string;
  password?: string;
  dateNaissance?: string;
  sexe?: string;
  gouvernorat?: string;
  poste?: string;
  nomSociete?: string;
  skills?: string;
  cvGenerique?: string;
}
