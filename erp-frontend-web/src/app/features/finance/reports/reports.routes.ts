import { Routes } from '@angular/router';

export const REPORTS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./reports-dashboard.component').then(m => m.ReportsDashboardComponent) },
  { path: 'trial-balance', loadComponent: () => import('./trial-balance.component').then(m => m.TrialBalanceComponent) },
  { path: 'balance-sheet', loadComponent: () => import('./balance-sheet.component').then(m => m.BalanceSheetComponent) },
  { path: 'income-statement', loadComponent: () => import('./income-statement.component').then(m => m.IncomeStatementComponent) },
];
