import { Routes } from '@angular/router';

export const TIME_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./time-sheets.component').then(m => m.TimeSheetsComponent) },
  { path: 'entries', loadComponent: () => import('./time-entries.component').then(m => m.TimeEntriesComponent) },
];
