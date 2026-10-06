import { Component } from '@angular/core';
import { ChildrenOutletContexts, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { routeFadeAnimation } from '../../animations/route.animations';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  animations: [routeFadeAnimation],
  template: `
    <header class="app-header">
      <div class="header-inner">
        <a routerLink="/inventory" class="brand">
          <span class="brand-mark"></span>
          setupmatch
        </a>
        <nav>
          <a routerLink="/inventory" routerLinkActive="active">Inventory</a>
          <a routerLink="/allocations" routerLinkActive="active">Allocations</a>
        </nav>
      </div>
    </header>
    <main class="app-main">
      <div class="route-container" [@routeFade]="routeAnimationState()">
        <router-outlet />
      </div>
    </main>
  `,
  styles: `
    .app-header {
      background: var(--color-surface);
      box-shadow: var(--shadow-sm);
      min-height: 64px;
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .header-inner {
      max-width: 1200px;
      margin: 0 auto;
      padding: 12px 24px;
      min-height: 64px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-family: var(--font-sans);
      font-weight: 600;
      font-size: 18px;
      letter-spacing: -0.02em;
      color: var(--color-text);
      text-decoration: none;
      flex-shrink: 0;
    }

    .brand-mark {
      width: 28px;
      height: 28px;
      border-radius: var(--radius-sm);
      background: linear-gradient(135deg, #0075eb 0%, #5b4cff 100%);
      display: inline-block;
    }

    nav {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      padding: 4px;
      background: var(--color-surface-muted);
      border-radius: var(--radius-pill);
    }

    nav a {
      color: var(--color-text-secondary);
      text-decoration: none;
      font-weight: 500;
      font-size: 14px;
      padding: 8px 16px;
      min-height: 40px;
      display: inline-flex;
      align-items: center;
      border-radius: var(--radius-pill);
      transition: background 0.15s ease, color 0.15s ease;
    }

    nav a:hover {
      color: var(--color-text);
      background: rgba(255, 255, 255, 0.7);
    }

    nav a.active {
      color: var(--color-primary);
      background: var(--color-surface);
      box-shadow: var(--shadow-sm);
    }

    .app-main {
      max-width: 1200px;
      margin: 0 auto;
      padding: 28px 24px 48px;
    }

    .route-container {
      display: block;
      position: relative;
      min-height: 200px;
    }

    @media (max-width: 767px) {
      .header-inner {
        padding: 12px 16px;
        flex-direction: column;
        align-items: stretch;
      }

      .app-main {
        padding: 20px 16px 40px;
      }

      nav {
        justify-content: stretch;
      }

      nav a {
        flex: 1;
        justify-content: center;
        font-size: 14px;
      }
    }
  `,
})
export class ShellComponent {
  constructor(private readonly outletContexts: ChildrenOutletContexts) {}

  routeAnimationState(): string {
    return this.outletContexts.getContext('primary')?.route?.snapshot.url.map((segment) => segment.path).join('/') ?? '';
  }
}
