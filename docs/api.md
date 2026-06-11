# Conventions API REST

## Base URL

```text
/api
```

## Authentification

```text
POST /api/auth/login
POST /api/auth/register
GET  /api/auth/verify-email?token=...
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/accept-invitation
GET  /api/auth/me
```

Les appels proteges utilisent un JWT dans l'en-tete :

```text
Authorization: Bearer <token>
```

Le backend recupere l'identite de l'utilisateur depuis le JWT. Les clients ne doivent pas envoyer d'identifiant utilisateur pour prendre en charge, escalader, resoudre ou cloturer un ticket.

Les refresh tokens, tokens de verification email et tokens d'invitation sont opaques, renouveles ou consommes a usage unique, et stockes cote serveur uniquement sous forme de hash.

Inscription demandeur :

```json
{
  "nom": "Benali",
  "prenom": "Yassine",
  "email": "yassine.benali@example.com",
  "password": "MotDePasseFort!2026"
}
```

Le role est toujours force a `DEMANDEUR`. L'utilisateur doit confirmer son adresse email avant de pouvoir se connecter.

Acceptation d'invitation :

```json
{
  "token": "token-recu-par-email",
  "password": "MotDePasseFort!2026"
}
```

## Utilisateurs

```text
GET  /api/users
GET  /api/users/{id}
POST /api/users
POST /api/users/invitations
```

Ces endpoints sont reserves au role `ADMIN`. Le premier administrateur doit etre cree manuellement dans la base de donnees avec un mot de passe BCrypt, afin d'eviter tout compte automatique ou secret par defaut dans le code.

Creation d'utilisateur :

```json
{
  "nom": "Dupont",
  "prenom": "Sara",
  "email": "sara.dupont@example.com",
  "password": "MotDePasseFort!2026",
  "role": "TECH_N1"
}
```

Invitation technicien ou administrateur :

```json
{
  "nom": "Alami",
  "prenom": "Nora",
  "email": "nora.alami@example.com",
  "role": "TECH_N1"
}
```

Le systeme envoie un email SMTP contenant un lien d'acceptation. Aucun mot de passe n'est envoye par email.

## Base de connaissances N0

```text
GET   /api/knowledge/articles
POST  /api/knowledge/articles
GET   /api/knowledge/articles/{id}
PATCH /api/knowledge/articles/{id}
POST  /api/knowledge/imports
GET   /api/knowledge/semantic/model
GET   /api/knowledge/semantic/articles/{id}/triples
GET   /api/knowledge/semantic/articles/{id}/reasoning
POST  /api/knowledge/semantic/sparql
```

Ces endpoints sont reserves au role `ADMIN`. Les articles alimentent N0 sans modifier le code ni les migrations applicatives. A chaque creation, import ou mise a jour, le backend regenere les chunks utilises par la recherche N0.

Creation ou mise a jour :

```json
{
  "titre": "Connexion VPN - erreur 809",
  "categorie": "RESEAU",
  "contenu": "Procedure de diagnostic validee par l'equipe support...",
  "motsCles": "vpn,erreur 809,ikev2,pare-feu",
  "actif": true
}
```

Import documentaire :

```text
POST /api/knowledge/imports
Content-Type: multipart/form-data

file=<procedure.txt|procedure.md>
categorie=RESEAU
motsCles=vpn,erreur 809,ikev2,pare-feu
```

Regles d'import :

- fichiers acceptes en premiere version : `.txt`, `.md` ;
- taille maximale : 2 Mo ;
- encodage attendu : UTF-8 ;
- extraction des sections a partir des titres Markdown ou lignes terminees par `:` ;
- sections reconnues : symptomes, causes, prerequis, procedure, verification, escalade ;
- l'article importe est cree comme brouillon inactif (`actif=false`) ;
- les chunks heritent du type de section pour privilegier les procedures lors de la recherche N0 ;
- un administrateur doit relire, corriger et activer l'article avant utilisation par N0.

API Semantic Web :

- `/api/knowledge/semantic/model` retourne le graphe RDF actif en Turtle.
- `/api/knowledge/semantic/articles/{id}/triples` retourne les triples RDF d'un article.
- `/api/knowledge/semantic/articles/{id}/reasoning` retourne les symptomes, causes, solutions, verifications et niveau d'escalade deduits des sections.
- `/api/knowledge/semantic/sparql` execute une requete SPARQL `SELECT` admin-only sur le modele RDF genere depuis les connaissances actives.

Exemple SPARQL :

```json
{
  "query": "select ?article ?level where { ?article itsm:escalatesTo ?level }"
}
```

## Tickets

```text
GET    /api/tickets
POST   /api/tickets
GET    /api/tickets/{id}
PATCH  /api/tickets/{id}
POST   /api/tickets/{id}/claim
POST   /api/tickets/{id}/escalate
POST   /api/tickets/{id}/resolve
POST   /api/tickets/{id}/close
GET    /api/tickets/{id}/events
```

Creation de ticket :

```json
{
  "titre": "Impossible de se connecter au VPN",
  "description": "Le client VPN affiche une erreur lors de la connexion.",
  "categorie": "RESEAU",
  "priorite": "NORMALE"
}
```

Escalade :

```json
{
  "raison": "Resolution distante impossible apres diagnostic N1."
}
```

## Files de support

```text
GET /api/queues/n1/tickets
GET /api/queues/n2/tickets
GET /api/queues/n3/tickets
```

## Chatbot N0

```text
POST /api/chatbot/sessions
POST /api/chatbot/sessions/{id}/messages
GET  /api/chatbot/sessions/{id}/messages
POST /api/chatbot/sessions/{id}/confirm-resolution
POST /api/chatbot/sessions/{id}/escalate
```

Creation de session :

```json
{
  "messageInitial": "Je n'arrive pas a me connecter au VPN, erreur 809."
}
```

Message utilisateur :

```json
{
  "message": "Le probleme concerne le VPN avec erreur 809."
}
```

Reponse N0 :

```json
{
  "session": {
    "id": "uuid-session",
    "statut": "OUVERTE",
    "categorieDetectee": "RESEAU",
    "ticketId": null
  },
  "reponse": {
    "auteur": "BOT",
    "contenu": "Voici une procedure documentee a essayer...",
    "sourcesUtilisees": "Connexion VPN - controles de base v1",
    "confidenceScore": 0.75
  },
  "confidenceScore": 0.75,
  "escaladeRecommandee": false,
  "sources": ["Connexion VPN - controles de base v1"],
  "ticket": null
}
```

Regles N0 :

- N0 repond uniquement depuis les articles actifs de la base de connaissances.
- Si le score de confiance est insuffisant, N0 recommande l'escalade.
- L'escalade N0 cree un ticket dans la file N1. Elle ne peut pas aller directement vers N2 ou N3.
- La conversation est conservee dans l'historique de session et reprise dans la description du ticket cree.

## Interventions

```text
GET    /api/interventions
POST   /api/interventions
GET    /api/interventions/{id}
GET    /api/interventions/ticket/{ticketId}
POST   /api/interventions/{id}/start
POST   /api/interventions/{id}/complete
POST   /api/interventions/{id}/cancel
```

Creation d'intervention :

```json
{
  "ticketId": "uuid-ticket",
  "technicienId": "uuid-technicien-n2-ou-n3",
  "dateDebutPrevue": "2026-06-10T09:00:00Z",
  "dateFinPrevue": "2026-06-10T11:00:00Z",
  "lieu": "Bureau 204"
}
```

Cloture d'intervention :

```json
{
  "rapport": "Diagnostic effectue sur site. Poste redemarre et connectique verifiee."
}
```

## Stock

```text
GET    /api/spare-parts
POST   /api/spare-parts
GET    /api/spare-parts/{id}
PATCH  /api/spare-parts/{id}
POST   /api/spare-parts/{id}/stock-movements
GET    /api/spare-parts/{id}/stock-movements
GET    /api/stock-alerts
```

Creation de piece :

```json
{
  "reference": "SSD-512-SATA",
  "nom": "SSD 512 Go SATA",
  "description": "Disque de remplacement pour postes utilisateurs",
  "quantiteInitiale": 10,
  "seuilAlerte": 2
}
```

Mouvement de stock :

```json
{
  "type": "SORTIE",
  "quantite": 1,
  "interventionId": "uuid-intervention",
  "commentaire": "Remplacement du disque defectueux"
}
```

Regles principales :

- `ADMIN` gere le catalogue et les entrees/ajustements de stock.
- `TECH_N1`, `TECH_N2` et `TECH_N3` consultent le catalogue et les alertes de stock.
- `TECH_N3` peut consommer une piece avec une sortie liee a une intervention.
- Les sorties ne peuvent pas rendre le stock negatif.
- Une alerte est exposee lorsque `quantiteDisponible <= seuilAlerte`.

## Tableaux de bord

```text
GET /api/dashboard/admin
GET /api/dashboard/technician
GET /api/dashboard/requester
```

Les tableaux de bord exposent des KPI deja agreges pour Angular/Chart.js. Les donnees sont filtrees cote backend selon le role connecte.

`ADMIN` :

```json
{
  "totalTickets": 42,
  "ticketsOuverts": 8,
  "ticketsResolus": 21,
  "totalInterventions": 12,
  "piecesEnAlerte": 3,
  "ticketsParStatut": [
    { "libelle": "OUVERT", "total": 8 }
  ],
  "ticketsParNiveau": [
    { "libelle": "N1", "total": 15 }
  ],
  "interventionsParStatut": [
    { "libelle": "PLANIFIEE", "total": 4 }
  ]
}
```

`TECH_N1`, `TECH_N2`, `TECH_N3` :

```json
{
  "ticketsAssignes": 6,
  "ticketsEnCours": 3,
  "ticketsFileNiveau": 9,
  "interventionsPlanifiees": 2,
  "piecesEnAlerte": 3,
  "ticketsAssignesParStatut": [],
  "interventionsParStatut": []
}
```

`DEMANDEUR` :

```json
{
  "totalTickets": 5,
  "ticketsOuverts": 1,
  "ticketsResolus": 2,
  "ticketsParStatut": []
}
```

## Notifications

```text
GET  /api/notifications
POST /api/notifications/{id}/read
```

Les notifications sont stockees en base et publiees en temps reel lorsque l'utilisateur est connecte en WebSocket.

## WebSocket

Canaux proposes :

```text
/ws
/topic/tickets/{ticketId}
/topic/queues/{level}
/user/queue/notifications
/topic/dashboard
```

Exemples :

- `/topic/tickets/{ticketId}` : changements visibles sur un ticket.
- `/topic/queues/n1` : mise a jour de la file N1.
- `/user/queue/notifications` : notifications privees de l'utilisateur authentifie.
