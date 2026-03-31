import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'candidat',
    loadComponent: () => import('./components/candidate-offers/candidate-offers.component').then(m => m.CandidateOffersComponent),
    children: [
      { path: 'offres',      loadComponent: () => import('./components/all-offers/all-offers.component').then(m => m.AllOffersComponent) },
      { path: 'mes-offres',  loadComponent: () => import('./components/my-offers/my-offers.component').then(m => m.MyOffersComponent) },
      { path: '',            redirectTo: 'offres', pathMatch: 'full' }
    ]
  },
  { path: '',         redirectTo: 'auth/login', pathMatch: 'full' },
  { path: '**',       redirectTo: 'auth/login' }
];
