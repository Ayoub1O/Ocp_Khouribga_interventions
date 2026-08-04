import { TicketCategory } from '../tickets/tickets.models';

export interface KnowledgeArticle {
  id: string;
  titre: string;
  categorie: TicketCategory;
  contenu: string;
  motsCles: string;
  sourceType: string;
  sourceNom: string | null;
  actif: boolean;
  version: number;
  dateCreation: string;
  dateDerniereModification: string;
}

export interface KnowledgeArticleRequest {
  titre: string;
  categorie: TicketCategory;
  contenu: string;
  motsCles: string;
  actif: boolean;
}

export interface KnowledgeImportResponse {
  article: KnowledgeArticle;
  chunksGeneres: number;
  message: string;
}

export interface EmbeddingReindexResponse {
  chunksTraites: number;
  embeddingsGeneres: number;
  erreurs: number;
  fournisseurConfigure: boolean;
}

export interface SemanticReasoning {
  articleId: string;
  articleTitre: string;
  categorie: string;
  niveauEscalade: string | null;
  symptomes: string[];
  causes: string[];
  solutions: string[];
  verifications: string[];
  reglesEscalade: string[];
}

export interface SparqlQueryResponse {
  variables: string[];
  rows: Record<string, string>[];
}
