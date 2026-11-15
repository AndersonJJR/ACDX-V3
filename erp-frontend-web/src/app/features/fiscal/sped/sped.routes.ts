import { Routes } from '@angular/router';

export const SPED_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./sped-dashboard.component').then(m => m.SpedDashboardComponent) },
  { path: 'efd-icms-ipi', loadComponent: () => import('./efd-icms-ipi.component').then(m => m.EfdIcmsIpiComponent) },
  { path: 'efd-contribuicoes', loadComponent: () => import('./efd-contribuicoes.component').then(m => m.EfdContribuicoesComponent) },
];
