import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../shared/components/sidebar/sidebar';

@Component({
  selector: 'app-shell-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, Sidebar],
  template: `
    <div class="app-shell">
      <app-sidebar></app-sidebar>
      <main class="content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-shell { display: grid; grid-template-columns: 280px 1fr; min-height: 100vh; }
    .content { background: #fbf7ef; }
    @media (max-width: 1024px) {
      .app-shell { grid-template-columns: 1fr; }
    }
  `]
})
export class ShellLayout {}
