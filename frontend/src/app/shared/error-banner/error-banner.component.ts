import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-error-banner',
  standalone: true,
  imports: [MatButtonModule],
  template: `
    <div class="error-banner">
      <div>
        <strong>Something went wrong</strong>
        <p>{{ message }}</p>
      </div>
      <button mat-button class="retry-btn" (click)="retry.emit()">Retry</button>
    </div>
  `,
  styles: `
    .error-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 16px;
      background: var(--color-error-bg);
      border-left: 4px solid var(--color-error-text);
      border-radius: var(--radius-sm);
      margin-bottom: 16px;
    }

    p {
      margin: 4px 0 0;
      color: var(--color-text-secondary);
    }

    .retry-btn {
      color: var(--color-primary);
      flex-shrink: 0;
    }
  `,
})
export class ErrorBannerComponent {
  @Input({ required: true }) message = '';
  @Output() retry = new EventEmitter<void>();
}
