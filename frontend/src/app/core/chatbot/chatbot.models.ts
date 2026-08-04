import { Ticket } from '../tickets/tickets.models';

export type ChatbotMessageAuthor = 'UTILISATEUR' | 'BOT' | 'SYSTEME';
export type ChatbotSessionStatus = 'OUVERTE' | 'RESOLUE' | 'ESCALADEE';

export interface ChatbotSession {
  id: string;
  statut: ChatbotSessionStatus;
  categorieDetectee: string | null;
  ticketId: string | null;
  dateCreation: string;
  dateFermeture: string | null;
}

export interface ChatbotMessage {
  id: string;
  auteur: ChatbotMessageAuthor;
  contenu: string;
  sourcesUtilisees: string | null;
  confidenceScore: number | null;
  dateCreation: string;
}

export interface ChatbotAnswer {
  session: ChatbotSession;
  reponse: ChatbotMessage;
  confidenceScore: number;
  escaladeRecommandee: boolean;
  sources: string[];
  ticket: Ticket | null;
}
