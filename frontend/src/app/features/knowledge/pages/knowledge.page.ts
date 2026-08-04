import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  LucideAlertTriangle,
  LucideBookOpen,
  LucideBot,
  LucideCheckCircle2,
  LucideDatabase,
  LucideFileText,
  LucideNetwork,
  LucideRefreshCw,
  LucideSave,
  LucideUpload,
} from '@lucide/angular';
import { KnowledgeArticle, SemanticReasoning } from '../../../core/knowledge/knowledge.models';
import { KnowledgeService } from '../../../core/knowledge/knowledge.service';
import { TicketCategory } from '../../../core/tickets/tickets.models';

@Component({
  selector: 'app-knowledge-page',
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    LucideAlertTriangle,
    LucideBookOpen,
    LucideBot,
    LucideCheckCircle2,
    LucideDatabase,
    LucideFileText,
    LucideNetwork,
    LucideRefreshCw,
    LucideSave,
    LucideUpload,
  ],
  templateUrl: './knowledge.page.html',
  styleUrl: './knowledge.page.scss',
})
export class KnowledgePage implements OnInit {
  private readonly knowledgeService = inject(KnowledgeService);

  protected readonly categories: TicketCategory[] = [
    'MATERIEL',
    'LOGICIEL',
    'RESEAU',
    'COMPTE_ACCES',
    'EMAIL',
    'IMPRIMANTE',
    'SECURITE',
    'AUTRE',
  ];

  protected readonly articles = signal<KnowledgeArticle[]>([]);
  protected readonly selectedArticle = signal<KnowledgeArticle | null>(null);
  protected readonly reasoning = signal<SemanticReasoning | null>(null);
  protected readonly triples = signal<string>('');
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly importing = signal(false);
  protected readonly reindexing = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly selectedFile = signal<File | null>(null);

  protected articleForm = {
    titre: '',
    categorie: 'RESEAU' as TicketCategory,
    motsCles: '',
    contenu: '',
    actif: false,
  };

  protected importForm = {
    categorie: 'RESEAU' as TicketCategory,
    motsCles: '',
  };

  protected readonly activeArticles = computed(() => this.articles().filter((article) => article.actif));
  protected readonly importedArticles = computed(() =>
    this.articles().filter((article) => article.sourceType === 'IMPORT'),
  );
  protected readonly categoriesCount = computed(() => new Set(this.articles().map((article) => article.categorie)).size);

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.knowledgeService.listArticles().subscribe({
      next: (articles) => {
        this.articles.set(articles);
        this.loading.set(false);
        if (!this.selectedArticle() && articles.length > 0) {
          this.selectArticle(articles[0]);
        }
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger la base de connaissances.');
      },
    });
  }

  protected selectArticle(article: KnowledgeArticle): void {
    this.selectedArticle.set(article);
    this.reasoning.set(null);
    this.triples.set('');
    this.articleForm = {
      titre: article.titre,
      categorie: article.categorie,
      motsCles: article.motsCles,
      contenu: article.contenu,
      actif: article.actif,
    };
    this.loadReasoning(article.id);
  }

  protected newArticle(): void {
    this.selectedArticle.set(null);
    this.reasoning.set(null);
    this.triples.set('');
    this.articleForm = {
      titre: '',
      categorie: 'RESEAU',
      motsCles: '',
      contenu: '',
      actif: false,
    };
  }

  protected saveArticle(): void {
    if (!this.articleForm.titre.trim() || !this.articleForm.contenu.trim() || this.saving()) {
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    const request = {
      titre: this.articleForm.titre.trim(),
      categorie: this.articleForm.categorie,
      motsCles: this.articleForm.motsCles.trim(),
      contenu: this.articleForm.contenu.trim(),
      actif: this.articleForm.actif,
    };
    const selected = this.selectedArticle();
    const operation = selected
      ? this.knowledgeService.updateArticle(selected.id, request)
      : this.knowledgeService.createArticle(request);

    operation.subscribe({
      next: (article) => {
        this.saving.set(false);
        this.success.set(selected ? 'Article mis a jour.' : 'Article cree.');
        this.selectedArticle.set(article);
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Enregistrement impossible. Verifiez le contenu de l article.');
      },
    });
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.item(0) ?? null);
  }

  protected importDocument(): void {
    const file = this.selectedFile();
    if (!file || this.importing()) {
      return;
    }

    this.importing.set(true);
    this.error.set(null);
    this.success.set(null);

    this.knowledgeService.importDocument(file, this.importForm.categorie, this.importForm.motsCles.trim()).subscribe({
      next: (result) => {
        this.importing.set(false);
        this.success.set(`${result.message} Chunks generes: ${result.chunksGeneres}.`);
        this.selectedArticle.set(result.article);
        this.load();
      },
      error: () => {
        this.importing.set(false);
        this.error.set('Import impossible. Utilisez un fichier texte ou Markdown lisible.');
      },
    });
  }

  protected reindexEmbeddings(): void {
    if (this.reindexing()) {
      return;
    }

    this.reindexing.set(true);
    this.error.set(null);
    this.success.set(null);

    this.knowledgeService.reindexEmbeddings().subscribe({
      next: (result) => {
        this.reindexing.set(false);
        this.success.set(
          `Embeddings: ${result.embeddingsGeneres}/${result.chunksTraites} generes, erreurs: ${result.erreurs}.`,
        );
      },
      error: () => {
        this.reindexing.set(false);
        this.error.set('Reindexation impossible. Verifiez la configuration Gemini/pgvector.');
      },
    });
  }

  protected loadReasoning(articleId = this.selectedArticle()?.id): void {
    if (!articleId) {
      return;
    }

    this.knowledgeService.getReasoning(articleId).subscribe({
      next: (reasoning) => this.reasoning.set(reasoning),
      error: () => this.reasoning.set(null),
    });
  }

  protected loadTriples(): void {
    const articleId = this.selectedArticle()?.id;
    if (!articleId) {
      return;
    }

    this.knowledgeService.getArticleTriples(articleId).subscribe({
      next: (triples) => this.triples.set(triples),
      error: () => this.error.set('Impossible de charger les triples RDF de cet article.'),
    });
  }

  protected categoryLabel(category: TicketCategory): string {
    return category.replace('_', ' ');
  }
}
