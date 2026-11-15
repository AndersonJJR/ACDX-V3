import { Routes } from '@angular/router';

export const NUMBERING_SERIES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./numbering-series-list.component').then(m => m.NumberingSeriesListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./numbering-series-form.component').then(m => m.NumberingSeriesFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./numbering-series-detail.component').then(m => m.NumberingSeriesDetailComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./numbering-series-form.component').then(m => m.NumberingSeriesFormComponent),
  },
];
