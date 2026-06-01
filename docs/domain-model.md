# Modele de domaine

## Entites principales

### User

Represente un utilisateur du systeme.

Champs principaux :

- `id`
- `nom`
- `prenom`
- `email`
- `motDePasseHash`
- `role`
- `actif`
- `dateCreation`

Roles :

- `DEMANDEUR`
- `TECH_N1`
- `TECH_N2`
- `TECH_N3`
- `ADMIN`

### Ticket

Represente un incident declare.

Champs principaux :

- `id`
- `reference`
- `titre`
- `description`
- `categorie`
- `priorite`
- `statut`
- `niveauCourant`
- `demandeur`
- `technicienAssigne`
- `dateCreation`
- `dateDerniereModification`
- `dateResolution`
- `dateCloture`

### TicketEvent

Historique immuable des actions realisees sur un ticket.

Exemples :

- `TICKET_CREE`
- `QUALIFIE_PAR_N0`
- `SOLUTION_N0_PROPOSEE`
- `RESOLU_PAR_N0`
- `ESCALADE_VERS_N1`
- `PRIS_EN_CHARGE`
- `ESCALADE_VERS_N2`
- `INTERVENTION_PLANIFIEE`
- `PIECE_CONSOMMEE`
- `RESOLU`
- `CLOTURE`

### Intervention

Represente une intervention terrain ou avancee.

Champs principaux :

- `id`
- `ticket`
- `technicien`
- `dateDebutPrevue`
- `dateFinPrevue`
- `dateDebutReelle`
- `dateFinReelle`
- `statut`
- `lieu`
- `rapport`

### SparePart

Piece de rechange geree en stock.

Champs principaux :

- `id`
- `reference`
- `nom`
- `description`
- `quantiteDisponible`
- `seuilAlerte`
- `actif`

### StockMovement

Trace une entree ou sortie de stock.

Champs principaux :

- `id`
- `piece`
- `type`
- `quantite`
- `intervention`
- `technicien`
- `dateMouvement`
- `commentaire`

## Relations principales

- Un `User` demandeur possede plusieurs `Ticket`.
- Un `Ticket` peut etre pris en charge par un technicien a la fois.
- Un `Ticket` possede plusieurs `TicketEvent`.
- Un `Ticket` peut posseder plusieurs `Intervention`.
- Une `Intervention` peut consommer plusieurs pieces via `StockMovement`.
- Une `SparePart` possede plusieurs mouvements de stock.

## Contraintes importantes

- La reference ticket doit etre unique.
- Un ticket cloture ne peut plus etre modifie sauf par action admin tracee.
- Une sortie de stock ne peut pas rendre la quantite disponible negative.
- Les articles de connaissance utilises par N0 doivent etre actifs et valides.

