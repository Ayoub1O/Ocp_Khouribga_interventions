import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TicketCategory } from '../tickets/tickets.models';
import {
  EmbeddingReindexResponse,
  KnowledgeArticle,
  KnowledgeArticleRequest,
  KnowledgeImportResponse,
  SemanticReasoning,
  SparqlQueryResponse,
} from './knowledge.models';

@Injectable({ providedIn: 'root' })
export class KnowledgeService {
  constructor(private readonly http: HttpClient) {}

  listArticles(): Observable<KnowledgeArticle[]> {
    return this.http.get<KnowledgeArticle[]>('/api/knowledge/articles');
  }

  createArticle(request: KnowledgeArticleRequest): Observable<KnowledgeArticle> {
    return this.http.post<KnowledgeArticle>('/api/knowledge/articles', request);
  }

  updateArticle(id: string, request: KnowledgeArticleRequest): Observable<KnowledgeArticle> {
    return this.http.patch<KnowledgeArticle>(`/api/knowledge/articles/${id}`, request);
  }

  importDocument(file: File, categorie: TicketCategory, motsCles: string): Observable<KnowledgeImportResponse> {
    const body = new FormData();
    body.append('file', file);
    body.append('categorie', categorie);
    body.append('motsCles', motsCles);
    return this.http.post<KnowledgeImportResponse>('/api/knowledge/imports', body);
  }

  reindexEmbeddings(): Observable<EmbeddingReindexResponse> {
    return this.http.post<EmbeddingReindexResponse>('/api/knowledge/embeddings/reindex', {});
  }

  getReasoning(articleId: string): Observable<SemanticReasoning> {
    return this.http.get<SemanticReasoning>(`/api/knowledge/semantic/articles/${articleId}/reasoning`);
  }

  getArticleTriples(articleId: string): Observable<string> {
    return this.http.get(`/api/knowledge/semantic/articles/${articleId}/triples`, { responseType: 'text' });
  }

  getActiveModel(): Observable<string> {
    return this.http.get('/api/knowledge/semantic/model', { responseType: 'text' });
  }

  querySparql(query: string): Observable<SparqlQueryResponse> {
    return this.http.post<SparqlQueryResponse>('/api/knowledge/semantic/sparql', { query });
  }
}
