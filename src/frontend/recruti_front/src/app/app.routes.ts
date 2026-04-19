import { Routes } from '@angular/router';
import { DashboardComponent } from './components/features/dashboard/dashboard.component';

export const routes: Routes = [
   {
    path: 'admin/dashboard',
    loadComponent: () => import('./components/features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
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
  {
      path:'home',
      loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {path: 'schedule-interview', loadComponent: () => import('./features/schedule-interview/schedule-interview.component').then(m => m.ScheduleInterviewComponent) },
  { path: '',         redirectTo: 'auth/login', pathMatch: 'full' },
  { path: '**',       redirectTo: 'auth/login' }
];
