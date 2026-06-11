# Assistant N0 et RAG hybride

## Role

L'assistant N0 est le premier point de contact. Il qualifie l'incident, guide le demandeur et propose des solutions issues de la base de connaissances validee.

Il ne remplace pas les techniciens et ne doit pas inventer de procedure.

## Architecture cible

```text
Interface chat Angular / Flutter
        |
ChatbotController
        |
ChatbotService
        |
+------------------------------+
| KnowledgeBaseService         |
| Retrieval / scoring          |
| TicketService                |
| NotificationService          |
+------------------------------+
        |
Base de connaissances + sections + chunks
```

## Implementation actuelle

La version actuelle n'utilise pas encore d'embeddings. Elle met en place la base robuste necessaire au RAG :

- import `.txt` et `.md` ;
- articles inactifs par defaut apres import ;
- validation administrateur ;
- extraction de sections ;
- generation de chunks ;
- recherche par categorie, mots-cles et type de section ;
- seuil de confiance ;
- escalade vers N1 si l'information est insuffisante.

## Type de RAG cible

L'evolution cible est un RAG hybride puis graphe + vector :

- recherche vectorielle pour la similarite semantique ;
- recherche texte pour les mots exacts, codes erreur, noms logiciels et references materiel ;
- filtres metadata pour la langue, categorie, niveau de support et statut de validation.
- graphe de connaissances pour relier symptomes, causes, solutions et niveau d'escalade.

Seuls les articles actifs, valides et autorises pour N0 peuvent etre utilises dans les reponses au demandeur.

## Strategie de reponse

1. Comprendre le message utilisateur.
2. Identifier les informations manquantes.
3. Recuperer les articles pertinents.
4. Verifier la confiance et la pertinence.
5. Repondre uniquement avec les contenus retrouves.
6. Demander si le probleme est resolu.
7. Cloturer ou escalader selon la confirmation.

## Anti-hallucination

Le RAG hybride reduit le risque d'hallucination, mais ne le supprime pas seul. La robustesse vient de la combinaison suivante :

- base de connaissances validee ;
- generation controlee ;
- seuils de confiance ;
- citations internes des articles utilises ;
- escalade vers N1 si l'information est insuffisante ;
- audit complet des interactions.

## Exemple de comportement

Utilisateur :

```text
Je n'arrive pas a me connecter au VPN.
```

N0 :

```text
Je vais vous aider a verifier le probleme VPN. Utilisez-vous un ordinateur professionnel connecte a Internet ?
```

Si les informations correspondent a un article valide, N0 propose une procedure simple. Sinon, il cree ou complete le ticket et l'envoie vers la file N1.
