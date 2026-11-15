import { Routes } from '@angular/router';

export const PURCHASES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./purchases-list.component').then(m => m.PurchasesListComponent) },
  { path: 'new', loadComponent: () => import('./purchase-form.component').then(m => m.PurchaseFormComponent) },
  { path: ':id', loadComponent: () => import('./purchase-detail.component').then(m => m.PurchaseDetailComponent) },
];
