# Vue d'architecture

## Vision generale

La plateforme est organisee autour d'un backend Spring Boot central qui expose des API REST securisees, des canaux WebSocket pour le temps reel, et une base PostgreSQL comme source de verite. Les clients Angular et Flutter consomment les memes contrats API.

```text
Angular Web App        Flutter Mobile App
      |                       |
      +----------+------------+
                 |
          REST API / WebSocket
                 |
          Spring Boot Backend
                 |
          PostgreSQL Database
```

## Modules backend

```text
auth
users
tickets
chatbot
knowledgebase
interventions
inventory
notifications
dashboard
audit
```

Chaque module suit une structure en couches :

```text
controller -> service -> repository -> entity
```

Les services portent les regles metier. Les controllers ne doivent pas contenir de logique de workflow.

## Modules frontend Angular

```text
core
shared
auth
tickets
chatbot
interventions
inventory
dashboard
admin
notifications
```

Les modules fonctionnels doivent etre charges paresseusement lorsque c'est pertinent. Les guards controlent l'acces selon le role.

## Mobile Flutter

L'application mobile cible surtout :

- suivi des tickets du demandeur ;
- consultation des files technicien ;
- prise en charge d'un ticket ;
- planning d'intervention ;
- redaction de rapports ;
- declaration des pieces consommees ;
- notifications.

Le mobile ne duplique pas la logique metier. Les decisions de workflow restent dans le backend.

## Decisions structurantes

- Le chatbot N0 assiste, mais ne remplace pas le workflow.
- Les escalades sont controlees par des regles deterministes.
- Les techniciens adoptent les tickets depuis leur file.
- Toute action importante genere un evenement d'audit.
- Les stocks sont modifies uniquement par des mouvements traces.
- Les notifications sont persistantes et diffusees en temps reel via WebSocket.

## Temps reel

Le backend expose un endpoint STOMP :

```text
/ws
```

Canaux principaux :

```text
/topic/tickets/{ticketId}
/topic/queues/{level}
/user/queue/notifications
```

Les clients Angular et Flutter peuvent s'abonner a ces canaux pour recevoir les mises a jour de tickets, files de support et notifications utilisateur.
