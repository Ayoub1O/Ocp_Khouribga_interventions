export type InterventionStatus = 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';

export interface Intervention {
  id: string;
  ticketId: string;
  ticketReference: string;
  ticketTitre: string;
  technicienId: string;
  technicienNomComplet: string;
  statut: InterventionStatus;
  dateDebutPrevue: string;
  dateFinPrevue: string;
  dateDebutReelle: string | null;
  dateFinReelle: string | null;
  lieu: string;
  rapport: string | null;
  dateCreation: string;
}

export interface CreateInterventionRequest {
  ticketId: string;
  technicienId: string;
  dateDebutPrevue: string;
  dateFinPrevue: string;
  lieu: string;
}

export interface CompleteInterventionRequest {
  rapport: string;
}

export interface CancelInterventionRequest {
  raison: string;
}
