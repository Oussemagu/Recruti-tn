export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
  dateNaissance: string;
  skills: string;
  sexe: string;
  gouvernorat: string;
  poste: string;
  nomSociete: string;
  cvGenerique: string;
}

export interface UpdateUserRequest {
  nom?: string;
  prenom?: string;
  email?: string;
  skills?: string;
  sexe?: string;
  gouvernorat?: string;
  poste?: string;
  nomSociete?: string;
  password?: string;
}