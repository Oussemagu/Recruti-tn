// offer.model.ts
export interface Offer {
  id?: number;
  dateEmission: string;
  titre: string;
  description: string;
  tags?: string;
  available: boolean;
  recruteurId?: number;
}
