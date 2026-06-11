# Architecture N0

## Objectif

N0 est l'assistant de premier contact du support IT. Son role est de qualifier l'incident, proposer une procedure documentee lorsque la base de connaissances le permet, puis cloturer la session ou escalader vers N1.

N0 n'est pas un chatbot libre. Il ne doit pas inventer de diagnostic, de procedure ou d'action technique. Toute reponse doit venir d'un contenu interne actif et valide.

## Flux Fonctionnel

```text
Demandeur
   |
   | message
   v
ChatbotController
   |
   v
ChatbotService
   |
   +--> detection categorie
   +--> recherche dans les chunks actifs
   +--> calcul score confiance
   +--> reponse documentee / clarification / recommandation escalade
   |
   v
chatbot_messages
```

Si le demandeur confirme la resolution, la session passe a `RESOLUE`. Si N0 ne peut pas resoudre, l'escalade cree un ticket via `TicketService`.

```text
N0 session non resolue
   |
   v
TicketService.create(...)
   |
   v
Ticket cree dans la file N1
```

L'escalade ne saute jamais de niveau :

```text
N0 -> N1 -> N2 -> N3
```

## Modules Backend

```text
n0/controller
  ChatbotController
  KnowledgeBaseController

n0/service
  ChatbotService
  KnowledgeBaseService

n0/domain
  ChatbotSession
  ChatbotMessage
  KnowledgeArticle
  KnowledgeSection
  KnowledgeChunk

n0/repository
  ChatbotSessionRepository
  ChatbotMessageRepository
  KnowledgeArticleRepository
  KnowledgeSectionRepository
  KnowledgeChunkRepository
```

## Modele De Connaissance

La base de connaissances est separee en trois niveaux.

```text
KnowledgeArticle
   |
   +-- KnowledgeSection
   |
   +-- KnowledgeChunk
```

`KnowledgeArticle` represente une procedure support validee ou importee. Il contient le titre, la categorie, le contenu complet, les mots-cles, l'etat actif/inactif, la version et l'origine.

`KnowledgeSection` represente une partie structuree du document :

```text
SYMPTOMES
CAUSES
PREREQUIS
PROCEDURE
VERIFICATION
ESCALADE
AUTRE
```

`KnowledgeChunk` est l'unite de recherche utilisee par N0. Les chunks heritent du type de section, ce qui permet de privilegier les contenus actionnables comme `PROCEDURE` et `VERIFICATION`.

## Import Documentaire

L'administrateur peut importer des documents `.txt` ou `.md`.

```text
POST /api/knowledge/imports
```

Le pipeline d'import :

```text
fichier upload
   |
   v
lecture UTF-8
   |
   v
nettoyage texte
   |
   v
detection titre
   |
   v
extraction sections
   |
   v
creation article inactif
   |
   v
generation sections + chunks
```

Les documents importes sont crees avec `actif=false`. Un administrateur doit relire, corriger et activer l'article avant que N0 puisse l'utiliser.

Cette decision evite qu'un document brut, incomplet ou non valide devienne directement une source de reponse utilisateur.

## Extraction Structuree

Le backend detecte les sections a partir de titres Markdown ou de lignes terminees par `:`.

Exemples reconnus :

```text
## Symptoms
## Cause
## Resolution
## Verification
## Escalation
```

```text
Symptomes:
Cause:
Procedure:
Escalade:
```

Le contenu est ensuite decoupe en chunks d'environ 900 caracteres, de preference sur des frontieres de phrases.

## Strategie De Recherche

La version actuelle utilise une recherche controlee par :

- categorie detectee dans le message utilisateur ;
- mots-cles de l'article ;
- type de section ;
- score de confiance.

Les chunks `PROCEDURE` et `VERIFICATION` sont favorises. Les chunks `ESCALADE` sont conserves, mais ne sont pas privilegies comme reponse principale.

Si le score est insuffisant, N0 recommande l'escalade vers N1.

## Anti-Hallucination

Les garde-fous actuels sont :

- N0 utilise uniquement les articles et chunks actifs ;
- les imports sont inactifs tant qu'ils ne sont pas valides ;
- les messages sont historises ;
- les sources utilisees sont stockees avec la reponse ;
- l'escalade cree un ticket par le workflow officiel ;
- N0 ne saute jamais directement vers N2 ou N3.

## Evolution Graph + Vector

La structure actuelle prepare l'evolution vers un RAG plus avance :

```text
KnowledgeChunk
   +--> embedding vectoriel
   +--> liens graphe symptome/cause/solution/escalade
```

Etapes futures :

1. Ajouter `pgvector` pour la similarite semantique.
2. Ajouter des tables graphe `knowledge_nodes` et `knowledge_edges`.
3. Lier les sections extraites aux noeuds `SYMPTOME`, `CAUSE`, `SOLUTION`, `SUPPORT_LEVEL`.
4. Combiner recherche mot-cle, recherche vectorielle et expansion graphe.

Cette evolution garde le meme contrat N0. Le changement se fait dans la couche retrieval, pas dans le workflow ticket.
