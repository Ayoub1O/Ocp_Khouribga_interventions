import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CancelInterventionRequest,
  CompleteInterventionRequest,
  CreateInterventionRequest,
  Intervention,
} from './interventions.models';

@Injectable({ providedIn: 'root' })
export class InterventionsService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>('/api/interventions');
  }

  create(request: CreateInterventionRequest): Observable<Intervention> {
    return this.http.post<Intervention>('/api/interventions', request);
  }

  start(id: string): Observable<Intervention> {
    return this.http.post<Intervention>(`/api/interventions/${id}/start`, {});
  }

  complete(id: string, request: CompleteInterventionRequest): Observable<Intervention> {
    return this.http.post<Intervention>(`/api/interventions/${id}/complete`, request);
  }

  cancel(id: string, request: CancelInterventionRequest): Observable<Intervention> {
    return this.http.post<Intervention>(`/api/interventions/${id}/cancel`, request);
  }
}
