import { InterventionSummary, SparePartSummary, TicketSummary } from './api.types';

export const tickets: TicketSummary[] = [
  {
    reference: 'INC-2026-0048',
    titre: 'Erreur VPN 809 sur poste distant',
    demandeur: 'Y. Benali',
    categorie: 'RESEAU',
    statut: 'EN_COURS',
    priorite: 'HAUTE',
    niveau: 'N1',
    technicien: 'S. Alami',
    age: '34 min',
  },
  {
    reference: 'INC-2026-0047',
    titre: 'Remplacement SSD requis',
    demandeur: 'N. Amrani',
    categorie: 'MATERIEL',
    statut: 'ESCALADE',
    priorite: 'CRITIQUE',
    niveau: 'N3',
    technicien: 'H. Idrissi',
    age: '2 h',
  },
  {
    reference: 'INC-2026-0046',
    titre: 'Boite Outlook ne synchronise plus',
    demandeur: 'M. Zahraoui',
    categorie: 'EMAIL',
    statut: 'OUVERT',
    priorite: 'NORMALE',
    niveau: 'N1',
    age: '3 h',
  },
  {
    reference: 'INC-2026-0045',
    titre: 'Imprimante bureau 204 hors service',
    demandeur: 'A. Kabbaj',
    categorie: 'IMPRIMANTE',
    statut: 'EN_COURS',
    priorite: 'NORMALE',
    niveau: 'N2',
    technicien: 'R. El Fassi',
    age: '5 h',
  },
];

export const interventions: InterventionSummary[] = [
  {
    id: 'INT-118',
    ticket: 'INC-2026-0047',
    technicien: 'H. Idrissi',
    lieu: 'Bureau 3.14',
    debut: '2026-07-04T09:30:00',
    fin: '2026-07-04T10:30:00',
    statut: 'PLANIFIEE',
  },
  {
    id: 'INT-119',
    ticket: 'INC-2026-0045',
    technicien: 'R. El Fassi',
    lieu: 'Bureau 204',
    debut: '2026-07-04T11:00:00',
    fin: '2026-07-04T12:00:00',
    statut: 'EN_COURS',
  },
  {
    id: 'INT-120',
    ticket: 'INC-2026-0043',
    technicien: 'M. Saidi',
    lieu: 'Salle reseau',
    debut: '2026-07-05T14:00:00',
    fin: '2026-07-05T16:00:00',
    statut: 'PLANIFIEE',
  },
];

export const spareParts: SparePartSummary[] = [
  { reference: 'SSD-512-SATA', nom: 'SSD 512 Go SATA', disponible: 1, seuil: 2, statut: 'ALERTE' },
  { reference: 'RAM-16-DDR4', nom: 'Barrette RAM 16 Go DDR4', disponible: 6, seuil: 3, statut: 'OK' },
  { reference: 'DOCK-USB-C', nom: 'Station USB-C', disponible: 2, seuil: 2, statut: 'ALERTE' },
  { reference: 'TONER-HP-410', nom: 'Toner HP 410', disponible: 0, seuil: 2, statut: 'ALERTE' },
];
