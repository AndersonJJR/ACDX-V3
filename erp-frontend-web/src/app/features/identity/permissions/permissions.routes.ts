import { Routes } from '@angular/router';

export const PERMISSIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./permissions-list.component').then(m => m.PermissionsListComponent),
  },
];
