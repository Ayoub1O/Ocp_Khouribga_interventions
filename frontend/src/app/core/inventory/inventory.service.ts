import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateSparePartRequest,
  CreateStockMovementRequest,
  SparePart,
  StockMovement,
  UpdateSparePartRequest,
} from './inventory.models';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  constructor(private readonly http: HttpClient) {}

  listParts(): Observable<SparePart[]> {
    return this.http.get<SparePart[]>('/api/spare-parts');
  }

  lowStockAlerts(): Observable<SparePart[]> {
    return this.http.get<SparePart[]>('/api/stock-alerts');
  }

  createPart(request: CreateSparePartRequest): Observable<SparePart> {
    return this.http.post<SparePart>('/api/spare-parts', request);
  }

  updatePart(id: string, request: UpdateSparePartRequest): Observable<SparePart> {
    return this.http.patch<SparePart>(`/api/spare-parts/${id}`, request);
  }

  createMovement(partId: string, request: CreateStockMovementRequest): Observable<StockMovement> {
    return this.http.post<StockMovement>(`/api/spare-parts/${partId}/stock-movements`, request);
  }
}
