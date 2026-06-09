# Plateforme interne de gestion des incidents IT

Cette application centralise la gestion du cycle de vie des incidents informatiques internes : qualification initiale, support multi-niveaux, interventions sur site, gestion des pieces de rechange, notifications et suivi des indicateurs.

Le produit final est concu en francais. La communication technique pendant le developpement peut rester en anglais, mais les libelles metier, les ecrans, les rapports et la documentation de presentation doivent utiliser le vocabulaire francais du domaine.

## Objectif

Mettre en place une plateforme ITSM interne capable de gerer :

- la creation et la qualification des tickets ;
- l'assistance N0 via chatbot avec RAG hybride ;
- les files de traitement N1, N2 et N3 ;
- la prise en charge volontaire des tickets par les techniciens ;
- l'escalade controlee entre niveaux de support ;
- la planification des interventions ;
- les rapports d'intervention ;
- le stock de pieces de rechange ;
- les notifications temps reel et email ;
- l'historique complet des actions.

## Parcours principal

```text
Demandeur
  -> Assistant virtuel N0
  -> Creation / qualification du ticket
  -> File N1
  -> Prise en charge par un technicien N1
  -> Resolution distante ou escalade
  -> File N2 pour diagnostic sur site
  -> File N3 pour intervention avancee / remplacement materiel
  -> Rapport, historique, resolution et cloture
```

## Niveaux de support

- **N0 - Assistant virtuel** : qualifie l'incident, cherche dans la base de connaissances validee, propose des solutions simples et escalade vers N1 si necessaire.
- **N1 - Support distant** : prend en charge les tickets depuis la file N1 et tente une resolution sans presence physique.
- **N2 - Diagnostic sur site** : intervient lorsque le diagnostic necessite une presence physique.
- **N3 - Intervention avancee** : gere les remplacements materiels, operations avancees et mouvements de stock.

Les tickets ne sont pas attribues par score ou competition. Chaque niveau possede sa file, et un technicien disponible adopte un ticket de son niveau.

## Stack technique

### Backend

- Java
- Spring Boot
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security + JWT
- Spring WebSocket
- Maven
- PostgreSQL

### Frontend web

- Angular
- TypeScript
- Angular Material
- RxJS
- FullCalendar
- Chart.js

### Mobile

- Flutter
- Consommation des memes API REST et WebSocket que le frontend web

## Documentation

- [Vue d'architecture](docs/architecture.md)
- [Modele de domaine](docs/domain-model.md)
- [Workflow des tickets](docs/ticket-workflow.md)
- [Assistant N0 et RAG hybride](docs/n0-rag.md)
- [Conventions API REST](docs/api.md)

## Configuration locale

Les identifiants reels de base de donnees ne doivent pas etre commits.

Le fichier versionne `backend/src/main/resources/application.yml` utilise des variables d'environnement :

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JPA_DDL_AUTO
SERVER_PORT
```

Pour une configuration locale, copier :

```text
backend/src/main/resources/application-local.example.yml
```

vers :

```text
backend/src/main/resources/application-local.yml
```

puis renseigner les identifiants locaux. Le fichier `application-local.yml` est ignore par Git.

Demarrage local :

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
