import { Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  template: `
    @for (item of items; track item) {
      <div class="skeleton-bone"
           [class]="'skeleton-' + variant()"
           [style.width]="width()"
           [style.height]="computedHeight()"
           [style.margin-bottom]="gap()">
      </div>
    }
  `,
  styles: [`
    .skeleton-bone {
      background: var(--border-light, #E8EBED);
      border-radius: 6px;
      position: relative;
      overflow: hidden;
    }
    .skeleton-bone::after {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: linear-gradient(
        90deg,
        transparent 0%,
        var(--border, rgba(255,255,255,0.4)) 50%,
        transparent 100%
      );
      animation: shimmer 1.5s infinite;
    }
    @keyframes shimmer {
      0% { transform: translateX(-100%); }
      100% { transform: translateX(100%); }
    }
    .skeleton-text { height: 16px; }
    .skeleton-card { height: 120px; }
    .skeleton-kpi { height: 80px; }
    .skeleton-circle { height: 40px; width: 40px; border-radius: 50%; }
  `]
})
export class SkeletonLoaderComponent {
  variant = input<'text' | 'card' | 'circle' | 'kpi'>('text');
  width = input<string>('100%');
  height = input<string>('');
  count = input<number>(1);
  gap = input<string>('8px');

  get items(): number[] {
    return Array.from({ length: this.count() }, (_, i) => i);
  }

  computedHeight(): string {
    if (this.height()) return this.height();
    const defaults: Record<string, string> = {
      text: '16px', card: '120px', kpi: '80px', circle: '40px'
    };
    return defaults[this.variant()] || '16px';
  }
}
