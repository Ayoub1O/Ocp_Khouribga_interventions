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

La version actuelle met en place un RAG hybride avec base documentaire, recherche vectorielle, Semantic Web et generation controlee :

- import `.txt` et `.md` ;
- articles inactifs par defaut apres import ;
- validation administrateur ;
- extraction de sections ;
- generation de chunks ;
- recherche par categorie, mots-cles et type de section ;
- embeddings Gemini des chunks ;
- stockage pgvector dans PostgreSQL ;
- recherche par similarite cosinus ;
- generation RDF avec Apache Jena ;
- requetes SPARQL admin-only ;
- raisonnement par relations RDF entre article, symptomes, causes, solutions et niveau d'escalade ;
- generation de reponse par Gemini apres sanitization ;
- utilisation des standards `rdf:type` et `rdfs:label`, avec proprietes metier `itsm:*` ;
- seuil de confiance ;
- escalade vers N1 uniquement apres confirmation utilisateur.

## Type de RAG cible

Le RAG utilise trois familles de signaux :

- recherche vectorielle pour la similarite semantique ;
- recherche texte pour les mots exacts, codes erreur, noms logiciels et references materiel ;
- filtres metadata pour la langue, categorie, niveau de support et statut de validation.
- graphe de connaissances pour relier symptomes, causes, solutions et niveau d'escalade.

Les articles actifs et valides sont prioritaires. Gemini peut ajouter des controles IT courants, mais le prompt lui interdit d'utiliser des sites externes ou de demander des secrets.

## Strategie de reponse

1. Comprendre le message utilisateur.
2. Identifier les informations manquantes.
3. Recuperer les articles pertinents.
4. Verifier la confiance et la pertinence.
5. Sanitiser les donnees sensibles avant appel API.
6. Generer une reponse francaise avec Gemini.
7. Demander si le probleme est resolu.
8. Cloturer ou escalader selon la confirmation utilisateur.

## Anti-hallucination

Le RAG hybride reduit le risque d'hallucination, mais ne le supprime pas seul. La robustesse vient de la combinaison suivante :

- base de connaissances validee ;
- generation controlee ;
- masquage emails, noms, IP privees, secrets et identifiants ;
- seuils de confiance ;
- citations internes des articles utilises ;
- escalade vers N1 recommandee si l'information est insuffisante, mais jamais automatique ;
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
