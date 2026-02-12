import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { SprintService } from '../../core/services/sprint.service';
import { Sprint } from '../../core/models/sprint.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProgressBarComponent } from '../../shared/components/progress-bar.component';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';
import { HoursPipe } from '../../shared/pipes/hours.pipe';

@Component({
  selector: 'app-sprint-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, StatusBadgeComponent, ProgressBarComponent, DatePtPipe, HoursPipe],
  template: `
    <h2>Sprints</h2>
    <div class="sprint-grid">
      @for (sprint of sprints; track sprint.id) {
        <mat-card class="sprint-card" [style.border-left-color]="sprint.color">
          <div class="sprint-header">
            <span class="sprint-num">Sprint {{ sprint.sprintNumber }}</span>
            <app-status-badge [status]="sprint.status" />
          </div>
          <h3>{{ sprint.name }}</h3>
          <p class="sprint-en">{{ sprint.nameEn }}</p>
          <div class="sprint-meta">
            <span>{{ sprint.startDate | datePt }} — {{ sprint.endDate | datePt }}</span>
            <span>{{ sprint.totalSessions }} sessões · {{ sprint.totalHours | hours }}</span>
          </div>
          <app-progress-bar [value]="sprint.progressPercent || 0" [color]="sprint.color" />
          <div class="sprint-stats">
            <span>{{ sprint.completedSessions }}/{{ sprint.totalSessions }} sessões</span>
            <span>{{ sprint.actualHours | hours }} / {{ sprint.totalHours | hours }}</span>
          </div>
          <p class="focus"><strong>Foco:</strong> {{ sprint.focus }}</p>
          <a mat-stroked-button [routerLink]="['/sprints', sprint.id]">Ver Detalhe</a>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .sprint-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    .sprint-card { padding: 20px; border-left: 4px solid; }
    .sprint-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
    .sprint-num { font-weight: 700; color: var(--text-secondary); }
    .sprint-card h3 { margin: 4px 0; }
    .sprint-en { color: var(--text-muted); font-size: 14px; margin: 0 0 12px; }
    .sprint-meta { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-secondary); margin-bottom: 12px; }
    .sprint-stats { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-secondary); margin-top: 8px; }
    .focus { font-size: 13px; color: var(--text-secondary); margin: 8px 0 12px; }

    @media (max-width: 768px) {
      .sprint-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class SprintListComponent implements OnInit {
  sprints: Sprint[] = [];

  constructor(private sprintService: SprintService) {}

  ngOnInit(): void {
    this.sprintService.findAll().subscribe(s => this.sprints = s);
  }
}
