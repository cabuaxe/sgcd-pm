import { Component, input, signal, effect, DestroyRef, inject } from '@angular/core';

@Component({
  selector: 'app-animated-counter',
  standalone: true,
  template: `<span class="counter">{{ prefix() }}{{ displayValue() }}{{ suffix() }}</span>`,
  styles: [`
    .counter {
      font-variant-numeric: tabular-nums;
    }
  `]
})
export class AnimatedCounterComponent {
  targetValue = input<number>(0);
  duration = input<number>(1000);
  decimals = input<number>(0);
  prefix = input<string>('');
  suffix = input<string>('');

  displayValue = signal('0');

  private animFrameId = 0;
  private destroyRef = inject(DestroyRef);

  constructor() {
    effect(() => {
      const target = this.targetValue();
      const dur = this.duration();
      const dec = this.decimals();

      this.cancelAnimation();

      if (target === 0) {
        this.displayValue.set((0).toFixed(dec));
        return;
      }

      const start = performance.now();
      const animate = (now: number) => {
        const elapsed = now - start;
        const progress = Math.min(elapsed / dur, 1);
        // ease-out-quad: t * (2 - t)
        const eased = progress * (2 - progress);
        const current = eased * target;
        this.displayValue.set(current.toFixed(dec));

        if (progress < 1) {
          this.animFrameId = requestAnimationFrame(animate);
        }
      };
      this.animFrameId = requestAnimationFrame(animate);
    });

    this.destroyRef.onDestroy(() => this.cancelAnimation());
  }

  private cancelAnimation(): void {
    if (this.animFrameId) {
      cancelAnimationFrame(this.animFrameId);
      this.animFrameId = 0;
    }
  }
}
