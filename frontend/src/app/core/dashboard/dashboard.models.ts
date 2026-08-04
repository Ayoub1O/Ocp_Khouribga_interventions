export interface CountByLabel {
  libelle: string;
  total: number;
}

export interface DailyTicketVolume {
  date: string;
  total: number;
}

export interface AdminDashboardData {
  totalTickets: number;
  ticketsOuverts: number;
  ticketsResolus: number;
  totalInterventions: number;
  piecesEnAlerte: number;
  ticketsParStatut: CountByLabel[];
  ticketsParNiveau: CountByLabel[];
  interventionsParStatut: CountByLabel[];
  volumeTicketsParJour: DailyTicketVolume[];
}

export interface TechnicianDashboardData {
  ticketsAssignes: number;
  ticketsEnCours: number;
  ticketsFileNiveau: number;
  interventionsPlanifiees: number;
  piecesEnAlerte: number;
  ticketsAssignesParStatut: CountByLabel[];
  interventionsParStatut: CountByLabel[];
}

export interface RequesterDashboardData {
  totalTickets: number;
  ticketsOuverts: number;
  ticketsResolus: number;
  ticketsParStatut: CountByLabel[];
  volumeTicketsParJour: DailyTicketVolume[];
}
