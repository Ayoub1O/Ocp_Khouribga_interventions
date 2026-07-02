# Architecture N0

## Objectif

N0 est l'assistant de premier contact du support IT. Son role est de qualifier l'incident, proposer une procedure documentee lorsque la base de connaissances le permet, puis cloturer la session ou escalader vers N1.

N0 n'est pas un chatbot libre. Il utilise en priorite les contenus internes actifs et valides. Le LLM peut reformuler et proposer des controles courants, mais il ne cree jamais de ticket seul et ne remplace pas la confirmation utilisateur.

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
   +--> sanitization donnees sensibles
   +--> recherche vectorielle pgvector
   +--> recherche mots-cles dans les chunks actifs
   +--> raisonnement RDF/Jena
   +--> generation Gemini controlee
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
  KnowledgeVectorService
  SemanticGraphService

n0/ai
  LlmClient
  GeminiLlmClient
  SensitiveDataSanitizer
  ChatbotAnswerGenerator

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

La version actuelle utilise une recherche hybride controlee par :

- categorie detectee dans le message utilisateur ;
- mots-cles de l'article ;
- type de section ;
- similarite vectorielle via `pgvector` ;
- raisonnement Semantic Web avec Apache Jena ;
- score de confiance.

Les chunks `PROCEDURE` et `VERIFICATION` sont favorises. Les chunks `ESCALADE` sont conserves, mais ne sont pas privilegies comme reponse principale.

Si le score est insuffisant, N0 recommande l'escalade vers N1.

## pgvector Et Embeddings Gemini

Chaque `KnowledgeChunk` peut avoir un embedding stocke dans `knowledge_chunk_embeddings`.

```text
knowledge_chunks.id
   |
   v
knowledge_chunk_embeddings.chunk_id
   |
   v
embedding vector(768)
```

Le modele d'embedding par defaut est `gemini-embedding-2` avec une dimension controlee a `768`. Le backend utilise la distance cosinus via l'operateur pgvector `<=>` pour retrouver les chunks proches de la question utilisateur.

Les embeddings sont generes :

- a la creation d'un article ;
- a l'import d'un document ;
- a la mise a jour d'un article ;
- manuellement via `POST /api/knowledge/embeddings/reindex` pour reindexer les chunks existants.

Si Gemini n'est pas configure, le backend continue de fonctionner avec la recherche par mots-cles et categorie.

## Gemini Generation

Gemini est utilise comme generateur de reponse, pas comme source de verite unique.

```text
Question utilisateur
   |
   v
SensitiveDataSanitizer
   |
   v
Retrieval: pgvector + mots-cles + RDF
   |
   v
Prompt controle
   |
   v
Gemini
   |
   v
Reponse francaise + recommandation eventuelle d'escalade
```

Donnees masquees avant l'appel API :

- emails ;
- numeros de telephone ;
- mots de passe, tokens, secrets et cles API ;
- IP privees ;
- identifiants longs comme numeros de serie ;
- nom et prenom de l'utilisateur connecte.

N0 peut recommander une escalade, mais seul `POST /api/chatbot/sessions/{id}/escalate` cree le ticket N1 apres confirmation utilisateur.

## Anti-Hallucination

Les garde-fous actuels sont :

- N0 utilise uniquement les articles et chunks actifs ;
- les imports sont inactifs tant qu'ils ne sont pas valides ;
- les messages sont historises ;
- les sources utilisees sont stockees avec la reponse ;
- l'escalade cree un ticket par le workflow officiel ;
- N0 ne saute jamais directement vers N2 ou N3.

## Evolution Graph + Vector

La structure actuelle inclut maintenant une couche Semantic Web avec Apache Jena :

```text
Articles actifs + sections
      |
      v
RDF Model Jena
      |
      v
SPARQL + raisonnement N0
```

Le vocabulaire RDF modelise :

- `KnowledgeArticle`
- `KnowledgeSection`
- `Symptom`
- `Cause`
- `Solution`
- `Verification`
- `EscalationRule`
- `TicketCategory`
- `SupportLevel`

Relations principales :

- `hasSymptom`
- `hasCause`
- `hasSolution`
- `hasVerification`
- `hasEscalationRule`
- `belongsToCategory`
- `escalatesTo`
- `documentedIn`

Les termes generiques utilisent les standards RDF/RDFS :

- `rdf:type` pour typer une ressource ;
- `rdfs:label` pour les libelles humains.

Les proprietes `itsm:*` sont reservees aux relations metier ITSM. Le projet conserve aussi une ontologie Turtle formelle de style OWL-lite :

```text
backend/src/main/resources/ontology/itsm.ttl
```

Cette ontologie declare :

- `owl:Ontology` pour identifier le vocabulaire ITSM ;
- `owl:Class` pour les concepts comme `Symptom`, `Cause`, `Solution`, `SupportLevel` ;
- `owl:ObjectProperty` pour les relations entre ressources ;
- `owl:DatatypeProperty` pour les valeurs litterales comme `sourceText` ;
- `rdfs:subClassOf`, `rdfs:domain`, `rdfs:range`, `rdfs:label` et `rdfs:comment` pour documenter la semantique.

Les namespaces sont separes :

```text
https://pfe.local/itsm/ontology#   classes et proprietes
https://pfe.local/itsm/resource/   instances generees
```

Exemple :

```text
itsm:Symptom      = classe
res:section-{id}  = instance issue d'une section importee
res:RESEAU        = instance de TicketCategory
res:N1            = instance de SupportLevel
```

N0 utilise ce graphe pour ajouter des indices de raisonnement, notamment le niveau d'escalade indique par les sections `ESCALADE`. Le workflow operationnel reste strictement sequentiel : N0 escalade toujours vers N1.

## Evolution Future

```text
KnowledgeChunk
   +--> embedding vectoriel
   +--> liens graphe symptome/cause/solution/escalade deja representes en RDF
```

Etapes futures :

1. Ajouter des documents PDF/HTML avec extraction plus avancee.
2. Ajouter un score de confiance plus formalise entre vectoriel, mots-cles et graphe.
3. Ajouter des tests d'evaluation N0 avec questions/reponses attendues.
4. Ajouter un journal d'audit specifique pour les donnees envoyees au fournisseur LLM.

Cette evolution garde le meme contrat N0. Le changement se fait dans la couche retrieval, pas dans le workflow ticket.
