import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminDashboardData, RequesterDashboardData, TechnicianDashboardData } from './dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly http: HttpClient) {}

  getAdminDashboard(): Observable<AdminDashboardData> {
    return this.http.get<AdminDashboardData>('/api/dashboard/admin');
  }

  getTechnicianDashboard(): Observable<TechnicianDashboardData> {
    return this.http.get<TechnicianDashboardData>('/api/dashboard/technician');
  }

  getRequesterDashboard(): Observable<RequesterDashboardData> {
    return this.http.get<RequesterDashboardData>('/api/dashboard/requester');
  }
}
