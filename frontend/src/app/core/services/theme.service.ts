import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'sgcd_pm_theme';
  readonly isDarkMode = signal(false);

  constructor() {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (stored === 'dark') {
      this.isDarkMode.set(true);
      document.body.classList.add('dark-mode');
    }
  }

  toggle(): void {
    const dark = !this.isDarkMode();
    this.isDarkMode.set(dark);
    document.body.classList.toggle('dark-mode', dark);
    localStorage.setItem(this.STORAGE_KEY, dark ? 'dark' : 'light');
  }
}
