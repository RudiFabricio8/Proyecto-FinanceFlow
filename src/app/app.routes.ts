import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { ShellLayout } from './layouts/shell/shell.layout';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },

  {
    path: '',
    component: ShellLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'payroll',
        loadComponent: () =>
          import('./features/payroll/payroll').then(m => m.Payroll)
      },
      {
        path: 'receipts',
        loadComponent: () =>
          import('./features/receipts/receipts').then(m => m.Receipts)
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('./features/analytics/analytics').then(m => m.Analytics)
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./features/notifications/notifications').then(m => m.Notifications)
      },
      {
        path: 'admin',
        loadComponent: () =>
          import('./features/admin/admin').then(m => m.Admin)
      },
      {
        path: 'help',
        loadComponent: () =>
          import('./features/help/help').then(m => m.Help)
      }
    ]
  },

  // Logout → Landing
  { path: 'logout', redirectTo: '/', pathMatch: 'full' },

  // Wildcard → Landing
  { path: '**', redirectTo: '/' }
];
