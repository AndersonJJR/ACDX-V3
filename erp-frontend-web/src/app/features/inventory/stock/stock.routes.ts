import { Routes } from '@angular/router';

export const STOCK_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./stock-dashboard.component').then(m => m.StockDashboardComponent) },
  { path: 'movements', loadComponent: () => import('./stock-movements.component').then(m => m.StockMovementsComponent) },
  { path: 'alerts', loadComponent: () => import('./stock-alerts.component').then(m => m.StockAlertsComponent) },
];
