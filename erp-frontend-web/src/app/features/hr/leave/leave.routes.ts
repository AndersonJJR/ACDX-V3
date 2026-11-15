import { Routes } from '@angular/router';

export const LEAVE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./leave-list.component').then(m => m.LeaveListComponent) },
  { path: 'new', loadComponent: () => import('./leave-form.component').then(m => m.LeaveFormComponent) },
  { path: ':id', loadComponent: () => import('./leave-detail.component').then(m => m.LeaveDetailComponent) },
];
