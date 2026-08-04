export type TicketStatus = 'OUVERT' | 'EN_COURS' | 'ESCALADE' | 'RESOLU' | 'CLOTURE';
export type SupportLevel = 'N1' | 'N2' | 'N3';
export type TicketPriority = 'BASSE' | 'NORMALE' | 'HAUTE' | 'CRITIQUE';

export interface TicketSummary {
  reference: string;
  titre: string;
  demandeur: string;
  categorie: string;
  statut: TicketStatus;
  priorite: TicketPriority;
  niveau: SupportLevel;
  technicien?: string;
  age: string;
}

export interface InterventionSummary {
  id: string;
  ticket: string;
  technicien: string;
  lieu: string;
  debut: string;
  fin: string;
  statut: 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE';
}

export interface SparePartSummary {
  reference: string;
  nom: string;
  disponible: number;
  seuil: number;
  statut: 'OK' | 'ALERTE';
}
