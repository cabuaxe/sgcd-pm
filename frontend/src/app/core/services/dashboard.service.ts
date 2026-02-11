import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Dashboard, StakeholderDashboard, CalendarData, BlockedDay, ProjectProgress } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private api: ApiService) {}

  getDeveloperDashboard(): Observable<Dashboard> {
    return this.api.get<Dashboard>('/dashboard');
  }

  getStakeholderDashboard(): Observable<StakeholderDashboard> {
    return this.api.get<StakeholderDashboard>('/dashboard/stakeholder');
  }

  getProjectProgress(): Observable<ProjectProgress> {
    return this.api.get<ProjectProgress>('/dashboard/progress');
  }

  getCalendar(year?: number, month?: number): Observable<CalendarData> {
    return this.api.get<CalendarData>('/calendar', { year, month });
  }

  getBlockedDays(): Observable<BlockedDay[]> {
    return this.api.get<BlockedDay[]>('/calendar/blocked');
  }
}
