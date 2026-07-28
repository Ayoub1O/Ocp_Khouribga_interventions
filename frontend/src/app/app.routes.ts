import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { AppShellComponent } from './core/layout/app-shell.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/pages/register.page').then((m) => m.RegisterPage),
  },
  {
    path: 'verify-email',
    loadComponent: () => import('./features/auth/pages/verify-email.page').then((m) => m.VerifyEmailPage),
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/pages/dashboard.page').then((m) => m.DashboardPage),
      },
      {
        path: 'tickets',
        loadComponent: () => import('./features/tickets/pages/tickets.page').then((m) => m.TicketsPage),
      },
      {
        path: 'queues',
        loadComponent: () => import('./features/queues/pages/queues.page').then((m) => m.QueuesPage),
      },
      {
        path: 'n0',
        loadComponent: () => import('./features/chatbot/pages/chatbot.page').then((m) => m.ChatbotPage),
      },
      {
        path: 'interventions',
        loadComponent: () => import('./features/interventions/pages/interventions.page').then((m) => m.InterventionsPage),
      },
      {
        path: 'inventory',
        loadComponent: () => import('./features/inventory/pages/inventory.page').then((m) => m.InventoryPage),
      },
      {
        path: 'reporting',
        loadComponent: () => import('./features/reporting/pages/reporting.page').then((m) => m.ReportingPage),
      },
      {
        path: 'admin',
        loadComponent: () => import('./features/admin/pages/admin.page').then((m) => m.AdminPage),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
