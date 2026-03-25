import { Routes } from '@angular/router';
//import { LandingComponent } from './features/landing/landing.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { authGuard } from './core/guards/auth-guards';
import { RecruiterOffersComponent } from './components/recruiter-offers/recruiter-offers.component';
import { CandidateOffersComponent } from './components/candidate-offers/candidate-offers.component';

export const routes: Routes = [
 // { path: '', component: LandingComponent },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'recruteur/offres', component: RecruiterOffersComponent , canActivate: [authGuard] },
  	{ path: 'candidat/offres', component: CandidateOffersComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: '' },
];