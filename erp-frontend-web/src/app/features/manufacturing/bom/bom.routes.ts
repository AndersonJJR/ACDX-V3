import { Routes } from '@angular/router';

export const BOM_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./bom-list.component').then(m => m.BomListComponent) },
  { path: 'new', loadComponent: () => import('./bom-form.component').then(m => m.BomFormComponent) },
  { path: ':id', loadComponent: () => import('./bom-detail.component').then(m => m.BomDetailComponent) },
];
