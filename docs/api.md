# Conventions API REST

## Base URL

```text
/api
```

## Authentification

```text
POST /api/auth/login
POST /api/auth/refresh
GET  /api/auth/me
```

Les appels proteges utilisent un JWT dans l'en-tete :

```text
Authorization: Bearer <token>
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
POST /api/chatbot/sessions/{id}/confirm-resolution
POST /api/chatbot/sessions/{id}/escalate
```

## Interventions

```text
GET    /api/interventions
POST   /api/interventions
GET    /api/interventions/{id}
PATCH  /api/interventions/{id}
POST   /api/interventions/{id}/start
POST   /api/interventions/{id}/complete
POST   /api/interventions/{id}/report
```

## Stock

```text
GET    /api/spare-parts
POST   /api/spare-parts
GET    /api/spare-parts/{id}
PATCH  /api/spare-parts/{id}
POST   /api/spare-parts/{id}/stock-movements
GET    /api/stock-alerts
```

## Notifications

```text
GET  /api/notifications
POST /api/notifications/{id}/read
```

## WebSocket

Canaux proposes :

```text
/topic/tickets/{ticketId}
/topic/queues/{level}
/user/queue/notifications
/topic/dashboard
```

