export type TicketCategory = 'MATERIEL' | 'LOGICIEL' | 'RESEAU' | 'COMPTE_ACCES' | 'EMAIL' | 'IMPRIMANTE' | 'SECURITE' | 'AUTRE';
export type TicketPriority = 'BASSE' | 'NORMALE' | 'HAUTE' | 'CRITIQUE';
export type TicketStatus = 'OUVERT' | 'EN_COURS' | 'ESCALADE' | 'RESOLU' | 'CLOTURE';
export type SupportLevel = 'N0' | 'N1' | 'N2' | 'N3';

export interface Ticket {
  id: string;
  reference: string;
  titre: string;
  description: string;
  categorie: TicketCategory;
  priorite: TicketPriority;
  statut: TicketStatus;
  niveauCourant: SupportLevel;
  demandeurId: string;
  demandeurNomComplet: string;
  demandeurTelephone: string | null;
  technicienAssigneId: string | null;
  dateCreation: string;
  dateDerniereModification: string;
}

export interface CreateTicketRequest {
  titre: string;
  description: string;
  categorie: TicketCategory;
  priorite: TicketPriority;
}

export interface EscalateTicketRequest {
  raison: string;
}

export interface ResolveTicketRequest {
  commentaire: string;
}

export interface CloseTicketRequest {
  commentaire: string;
}
