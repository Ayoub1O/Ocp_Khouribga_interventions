export type StockMovementType = 'ENTREE' | 'SORTIE' | 'AJUSTEMENT';

export interface SparePart {
  id: string;
  reference: string;
  nom: string;
  description: string | null;
  quantiteDisponible: number;
  seuilAlerte: number;
  actif: boolean;
  lowStock: boolean;
  dateCreation: string;
}

export interface CreateSparePartRequest {
  reference: string;
  nom: string;
  description: string;
  quantiteInitiale: number;
  seuilAlerte: number;
}

export interface UpdateSparePartRequest {
  nom: string;
  description: string;
  seuilAlerte: number;
  actif: boolean;
}

export interface CreateStockMovementRequest {
  type: StockMovementType;
  quantite: number;
  interventionId?: string | null;
  commentaire: string;
}

export interface StockMovement {
  id: string;
  pieceId: string;
  type: StockMovementType;
  quantite: number;
  interventionId: string | null;
  technicienId: string;
  commentaire: string;
  dateMouvement: string;
}
