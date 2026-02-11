import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule],
  template: `
    <div class="layout">
      <mat-toolbar class="header">
        <span class="logo">SGCD-PM</span>
        <span class="spacer"></span>
        <span class="user-info">{{ auth.getRole() }}</span>
        <button mat-icon-button (click)="auth.logout()">
          <mat-icon>logout</mat-icon>
        </button>
      </mat-toolbar>

      <div class="content-area">
        <nav class="sidebar">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
            <mat-icon>dashboard</mat-icon> Dashboard
          </a>
          <a routerLink="/progress" routerLinkActive="active">
            <mat-icon>trending_up</mat-icon> Progresso
          </a>
          <a routerLink="/sprints" routerLinkActive="active">
            <mat-icon>flag</mat-icon> Sprints
          </a>
          <a routerLink="/tasks" routerLinkActive="active">
            <mat-icon>task_alt</mat-icon> Tarefas
          </a>
          <a routerLink="/prompts" routerLinkActive="active">
            <mat-icon>smart_toy</mat-icon> Prompts
          </a>
          <a routerLink="/calendar" routerLinkActive="active">
            <mat-icon>calendar_month</mat-icon> Calendário
          </a>
          <a routerLink="/reports" routerLinkActive="active">
            <mat-icon>assessment</mat-icon> Relatórios
          </a>
          <div class="divider"></div>
          <a routerLink="/stakeholder" target="_blank">
            <mat-icon>visibility</mat-icon> Stakeholder
          </a>
        </nav>

        <main class="main-content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    .layout { display: flex; flex-direction: column; height: 100vh; }
    .header {
      background: var(--angola-black);
      color: white;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .logo {
      font-family: 'Playfair Display', serif;
      font-size: 20px;
      font-weight: 700;
      color: var(--angola-gold);
    }
    .spacer { flex: 1; }
    .user-info { margin-right: 8px; font-size: 14px; opacity: 0.8; }
    .content-area { display: flex; flex: 1; overflow: hidden; }
    .sidebar {
      width: 220px;
      background: var(--surface);
      border-right: 1px solid var(--border-light);
      display: flex;
      flex-direction: column;
      padding: 8px;
      overflow-y: auto;
    }
    .sidebar a {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      text-decoration: none;
      color: var(--text-secondary);
      border-radius: 8px;
      font-size: 14px;
      transition: all 0.2s;
    }
    .sidebar a:hover { background: var(--surface-alt); color: var(--text-primary); }
    .sidebar a.active { background: var(--angola-red); color: white; }
    .sidebar a.active mat-icon { color: white; }
    .divider { height: 1px; background: var(--border-light); margin: 8px 16px; }
    .main-content { flex: 1; overflow-y: auto; padding: 24px; }
  `]
})
export class LayoutComponent {
  constructor(public auth: AuthService) {}
}
