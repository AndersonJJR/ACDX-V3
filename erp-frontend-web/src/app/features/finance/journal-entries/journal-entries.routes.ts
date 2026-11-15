import { Routes } from '@angular/router';

export const JOURNAL_ENTRIES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./journal-entries-list.component').then(m => m.JournalEntriesListComponent) },
  { path: 'new', loadComponent: () => import('./journal-entry-form.component').then(m => m.JournalEntryFormComponent) },
  { path: ':id', loadComponent: () => import('./journal-entry-detail.component').then(m => m.JournalEntryDetailComponent) },
];
