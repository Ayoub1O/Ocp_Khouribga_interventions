# Workflow des tickets

## Etats

```text
OUVERT
EN_COURS
ESCALADE
RESOLU
CLOTURE
```

## Niveaux

```text
N0
N1
N2
N3
```

## Regles principales

1. Un demandeur declare un incident via formulaire ou assistant N0.
2. N0 qualifie l'incident et propose une solution si la base de connaissances le permet.
3. Si le demandeur confirme la resolution, le ticket passe a `RESOLU`, puis `CLOTURE`.
4. Si N0 ne peut pas resoudre, le ticket est place dans la file N1.
5. Un technicien N1 adopte le ticket lorsqu'il est disponible.
6. N1 resout a distance ou escalade vers N2.
7. N2 effectue le diagnostic sur site et resout ou escalade vers N3.
8. N3 gere les interventions avancees et les remplacements materiels.
9. Toute transition genere un evenement d'audit.

## Adoption d'un ticket

L'adoption est une action volontaire du technicien :

```text
POST /api/tickets/{id}/claim
```

Conditions :

- le ticket est dans la file du niveau du technicien ;
- le ticket n'est pas deja assigne ;
- le ticket n'est pas resolu ou cloture.

Effets :

- `technicienAssigne` est renseigne ;
- `statut` devient `EN_COURS` ;
- un evenement `PRIS_EN_CHARGE` est ajoute.

## Escalade

L'escalade deplace le ticket vers le niveau suivant.

```text
POST /api/tickets/{id}/escalate
```

Effets :

- le niveau courant est mis a jour ;
- l'ancien technicien est libere ;
- le ticket devient disponible dans la nouvelle file ;
- la raison d'escalade est historisee.

