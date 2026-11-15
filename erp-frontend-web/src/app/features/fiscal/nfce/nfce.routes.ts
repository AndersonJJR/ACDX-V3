import { Routes } from '@angular/router';

export const NFCE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./nfce-list.component').then(m => m.NfceListComponent) },
  { path: 'new', loadComponent: () => import('./nfce-form.component').then(m => m.NfceFormComponent) },
  { path: ':id', loadComponent: () => import('./nfce-detail.component').then(m => m.NfceDetailComponent) },
];
