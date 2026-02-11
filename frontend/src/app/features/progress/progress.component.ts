import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { DashboardService } from '../../core/services/dashboard.service';
import { ProjectProgress, SprintProgress } from '../../core/models/dashboard.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProgressBarComponent } from '../../shared/components/progress-bar.component';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';
import { HoursPipe } from '../../shared/pipes/hours.pipe';

@Component({
  selector: 'app-progress',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTableModule,
    StatusBadgeComponent, ProgressBarComponent, DatePtPipe, HoursPipe],
  template: `
    @if (data) {
      <h2>Progresso Detalhado do Projecto</h2>

      <!-- KPI Row -->
      <div class="kpi-row">
        <mat-card class="kpi-card">
          <div class="kpi-icon"><mat-icon>percent</mat-icon></div>
          <div class="kpi-label">Progresso Global</div>
          <div class="kpi-value">{{ data.overallProgress | number:'1.1-1' }}%</div>
          <app-progress-bar [value]="data.overallProgress" color="var(--angola-red)" />
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-icon"><mat-icon>event_note</mat-icon></div>
          <div class="kpi-label">Sessoes</div>
          <div class="kpi-value">{{ data.completedSessions }} / {{ data.totalSessions }}</div>
          <div class="kpi-sub">{{ data.totalSessions - data.completedSessions }} restantes</div>
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-icon"><mat-icon>schedule</mat-icon></div>
          <div class="kpi-label">Horas</div>
          <div class="kpi-value">{{ data.totalHoursSpent | hours }} / {{ data.totalHoursPlanned | hours }}</div>
          <app-progress-bar [value]="hoursPercent" color="var(--angola-gold)" />
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-icon"><mat-icon>speed</mat-icon></div>
          <div class="kpi-label">Velocidade</div>
          <div class="kpi-value">{{ data.avgSessionsPerWeek }} sessoes/sem</div>
          <div class="kpi-sub">{{ data.avgHoursPerWeek }}h/semana</div>
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-icon"><mat-icon>timer</mat-icon></div>
          <div class="kpi-label">Dias Restantes</div>
          <div class="kpi-value">{{ data.daysRemaining }}</div>
          <div class="kpi-sub">{{ data.weeksRemaining }} semanas</div>
        </mat-card>
      </div>

      <!-- Task Status Distribution -->
      <h3>Distribuicao de Tarefas</h3>
      <mat-card class="status-card">
        <div class="status-bar">
          @if (data.totalCompleted > 0) {
            <div class="status-segment completed" [style.width.%]="pct(data.totalCompleted)" title="Concluido: {{ data.totalCompleted }}"></div>
          }
          @if (data.totalInProgress > 0) {
            <div class="status-segment in-progress" [style.width.%]="pct(data.totalInProgress)" title="Em Progresso: {{ data.totalInProgress }}"></div>
          }
          @if (data.totalPlanned > 0) {
            <div class="status-segment planned" [style.width.%]="pct(data.totalPlanned)" title="Planeado: {{ data.totalPlanned }}"></div>
          }
          @if (data.totalBlocked > 0) {
            <div class="status-segment blocked" [style.width.%]="pct(data.totalBlocked)" title="Bloqueado: {{ data.totalBlocked }}"></div>
          }
          @if (data.totalSkipped > 0) {
            <div class="status-segment skipped" [style.width.%]="pct(data.totalSkipped)" title="Ignorado: {{ data.totalSkipped }}"></div>
          }
        </div>
        <div class="status-legend">
          <span class="legend-item"><span class="dot completed"></span> Concluido: {{ data.totalCompleted }}</span>
          <span class="legend-item"><span class="dot in-progress"></span> Em Progresso: {{ data.totalInProgress }}</span>
          <span class="legend-item"><span class="dot planned"></span> Planeado: {{ data.totalPlanned }}</span>
          <span class="legend-item"><span class="dot blocked"></span> Bloqueado: {{ data.totalBlocked }}</span>
          <span class="legend-item"><span class="dot skipped"></span> Ignorado: {{ data.totalSkipped }}</span>
        </div>
      </mat-card>

      <!-- Sprint Breakdown Table -->
      <h3>Detalhe por Sprint</h3>
      <mat-card class="table-card">
        <div class="table-scroll">
          <table mat-table [dataSource]="data.sprints">
            <ng-container matColumnDef="sprint">
              <th mat-header-cell *matHeaderCellDef>Sprint</th>
              <td mat-cell *matCellDef="let s">
                <div class="sprint-cell">
                  <span class="sprint-dot" [style.background]="s.color"></span>
                  <span class="sprint-num">S{{ s.sprintNumber }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let s">{{ s.name }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Estado</th>
              <td mat-cell *matCellDef="let s"><app-status-badge [status]="s.status" /></td>
            </ng-container>

            <ng-container matColumnDef="sessions">
              <th mat-header-cell *matHeaderCellDef>Sessoes</th>
              <td mat-cell *matCellDef="let s">{{ s.completedSessions }}/{{ s.totalSessions }}</td>
            </ng-container>

            <ng-container matColumnDef="hours">
              <th mat-header-cell *matHeaderCellDef>Horas</th>
              <td mat-cell *matCellDef="let s">{{ s.actualHours | hours }} / {{ s.totalHours | hours }}</td>
            </ng-container>

            <ng-container matColumnDef="tasks">
              <th mat-header-cell *matHeaderCellDef>Tarefas</th>
              <td mat-cell *matCellDef="let s">
                <div class="task-counts">
                  @if (s.completedTasks > 0) { <span class="tc completed">{{ s.completedTasks }}</span> }
                  @if (s.inProgressTasks > 0) { <span class="tc in-progress">{{ s.inProgressTasks }}</span> }
                  @if (s.plannedTasks > 0) { <span class="tc planned">{{ s.plannedTasks }}</span> }
                  @if (s.blockedTasks > 0) { <span class="tc blocked">{{ s.blockedTasks }}</span> }
                  @if (s.skippedTasks > 0) { <span class="tc skipped">{{ s.skippedTasks }}</span> }
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="progress">
              <th mat-header-cell *matHeaderCellDef>Progresso</th>
              <td mat-cell *matCellDef="let s" style="min-width: 140px;">
                <app-progress-bar [value]="s.progress" [color]="s.color" />
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="sprintColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: sprintColumns;"></tr>
          </table>
        </div>
      </mat-card>

      <!-- Hours Variance -->
      <h3>Variancia de Horas</h3>
      <mat-card class="variance-card">
        <div class="variance-grid">
          @for (s of data.sprints; track s.sprintNumber) {
            <div class="variance-item">
              <div class="variance-header">
                <span class="sprint-dot" [style.background]="s.color"></span>
                S{{ s.sprintNumber }}
              </div>
              <div class="variance-bars">
                <div class="var-row">
                  <span class="var-label">Planeado</span>
                  <div class="var-track">
                    <div class="var-fill planned-bg" [style.width.%]="hoursBarPct(s.totalHours)"></div>
                  </div>
                  <span class="var-val">{{ s.totalHours | hours }}</span>
                </div>
                <div class="var-row">
                  <span class="var-label">Real</span>
                  <div class="var-track">
                    <div class="var-fill actual-bg" [style.width.%]="hoursBarPct(s.actualHours || 0)"></div>
                  </div>
                  <span class="var-val">{{ (s.actualHours || 0) | hours }}</span>
                </div>
              </div>
              <div class="variance-delta" [class.positive]="(s.actualHours || 0) <= s.totalHours" [class.negative]="(s.actualHours || 0) > s.totalHours">
                {{ getDelta(s) }}
              </div>
            </div>
          }
        </div>
      </mat-card>

      <!-- Milestone Timeline -->
      <h3>Cronograma de Sprints</h3>
      <mat-card class="timeline-card">
        <div class="timeline">
          @for (s of data.sprints; track s.sprintNumber) {
            <div class="tl-item" [class.tl-completed]="s.status === 'COMPLETED'" [class.tl-active]="s.status === 'ACTIVE'">
              <div class="tl-dot" [style.border-color]="s.color">
                @if (s.status === 'COMPLETED') { <mat-icon>check</mat-icon> }
                @else if (s.status === 'ACTIVE') { <mat-icon>play_arrow</mat-icon> }
              </div>
              <div class="tl-content">
                <div class="tl-title">Sprint {{ s.sprintNumber }} — {{ s.name }}</div>
                <div class="tl-dates">{{ s.startDate | datePt:'medium' }} — {{ s.endDate | datePt:'medium' }}</div>
                <div class="tl-meta">{{ s.totalSessions }} sessoes &middot; {{ s.totalHours | hours }}</div>
              </div>
              <div class="tl-status">
                <app-status-badge [status]="s.status" />
              </div>
            </div>
          }
        </div>
      </mat-card>
    } @else {
      <p>A carregar dados de progresso...</p>
    }
  `,
  styles: [`
    h2 { margin: 0 0 20px; }
    h3 { margin: 24px 0 12px; }
    .kpi-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 8px; }
    .kpi-card { padding: 16px; position: relative; }
    .kpi-icon { position: absolute; top: 12px; right: 12px; opacity: 0.15; }
    .kpi-icon mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .kpi-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
    .kpi-value { font-size: 22px; font-weight: 700; margin: 4px 0; }
    .kpi-sub { font-size: 13px; color: var(--text-secondary); }

    /* Status bar */
    .status-card { padding: 20px; }
    .status-bar { display: flex; height: 28px; border-radius: 6px; overflow: hidden; margin-bottom: 12px; }
    .status-segment { transition: width 0.4s ease; }
    .status-segment.completed { background: #2EA043; }
    .status-segment.in-progress { background: #3884F4; }
    .status-segment.planned { background: #E8EBED; }
    .status-segment.blocked { background: #CC092F; }
    .status-segment.skipped { background: #8B949E; }
    .status-legend { display: flex; gap: 20px; flex-wrap: wrap; }
    .legend-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--text-secondary); }
    .dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
    .dot.completed { background: #2EA043; }
    .dot.in-progress { background: #3884F4; }
    .dot.planned { background: #E8EBED; }
    .dot.blocked { background: #CC092F; }
    .dot.skipped { background: #8B949E; }

    /* Sprint table */
    .table-card { padding: 0; overflow: hidden; }
    .table-scroll { overflow-x: auto; }
    table { width: 100%; }
    th { font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-muted); }
    td { font-size: 14px; }
    .sprint-cell { display: flex; align-items: center; gap: 8px; }
    .sprint-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
    .sprint-num { font-weight: 700; }
    .task-counts { display: flex; gap: 4px; }
    .tc {
      display: inline-block; padding: 2px 8px; border-radius: 10px;
      font-size: 12px; font-weight: 600;
    }
    .tc.completed { background: #D1FAE5; color: #2EA043; }
    .tc.in-progress { background: #DBEAFE; color: #3884F4; }
    .tc.planned { background: #E8EBED; color: #57606A; }
    .tc.blocked { background: #FEE2E2; color: #CC092F; }
    .tc.skipped { background: #F3F4F6; color: #8B949E; }

    /* Variance */
    .variance-card { padding: 20px; }
    .variance-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
    .variance-item { padding: 12px; border: 1px solid var(--border-light); border-radius: 8px; }
    .variance-header { display: flex; align-items: center; gap: 8px; font-weight: 700; margin-bottom: 8px; }
    .var-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
    .var-label { font-size: 12px; color: var(--text-muted); width: 60px; }
    .var-track { flex: 1; height: 8px; background: var(--border-light); border-radius: 4px; overflow: hidden; }
    .var-fill { height: 100%; border-radius: 4px; }
    .planned-bg { background: var(--angola-gold); }
    .actual-bg { background: var(--angola-red); }
    .var-val { font-size: 12px; color: var(--text-secondary); min-width: 36px; text-align: right; }
    .variance-delta { font-size: 13px; font-weight: 600; margin-top: 4px; text-align: right; }
    .variance-delta.positive { color: #2EA043; }
    .variance-delta.negative { color: #CC092F; }

    /* Timeline */
    .timeline-card { padding: 20px; }
    .timeline { display: flex; flex-direction: column; gap: 0; position: relative; padding-left: 24px; }
    .tl-item {
      display: flex; align-items: center; gap: 16px; padding: 12px 0;
      border-left: 2px solid var(--border-light); margin-left: -24px; padding-left: 40px;
      position: relative;
    }
    .tl-item:last-child { border-left-color: transparent; }
    .tl-dot {
      position: absolute; left: -13px; width: 24px; height: 24px;
      border-radius: 50%; border: 3px solid var(--border-light);
      background: var(--surface); display: flex; align-items: center; justify-content: center;
    }
    .tl-dot mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .tl-completed .tl-dot { background: #D1FAE5; }
    .tl-completed .tl-dot mat-icon { color: #2EA043; }
    .tl-active .tl-dot { background: #DBEAFE; }
    .tl-active .tl-dot mat-icon { color: #3884F4; }
    .tl-content { flex: 1; }
    .tl-title { font-weight: 600; font-size: 14px; }
    .tl-dates { font-size: 13px; color: var(--text-secondary); }
    .tl-meta { font-size: 12px; color: var(--text-muted); }

    @media (max-width: 1200px) {
      .kpi-row { grid-template-columns: repeat(3, 1fr); }
      .variance-grid { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 768px) {
      .kpi-row { grid-template-columns: repeat(2, 1fr); }
      .variance-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class ProgressComponent implements OnInit {
  data: ProjectProgress | null = null;
  sprintColumns = ['sprint', 'name', 'status', 'sessions', 'hours', 'tasks', 'progress'];
  private maxHours = 0;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getProjectProgress().subscribe(d => {
      this.data = d;
      this.maxHours = Math.max(...d.sprints.map(s => Math.max(s.totalHours, s.actualHours || 0)));
    });
  }

  get hoursPercent(): number {
    if (!this.data) return 0;
    return this.data.totalHoursPlanned > 0
      ? (this.data.totalHoursSpent * 100) / this.data.totalHoursPlanned : 0;
  }

  pct(count: number): number {
    if (!this.data) return 0;
    return (count * 100) / this.data.totalSessions;
  }

  hoursBarPct(hours: number): number {
    return this.maxHours > 0 ? (hours * 100) / this.maxHours : 0;
  }

  getDelta(s: SprintProgress): string {
    const delta = s.totalHours - (s.actualHours || 0);
    if (delta === 0) return '0h';
    return delta > 0 ? `-${delta}h restam` : `+${Math.abs(delta)}h excedido`;
  }
}
