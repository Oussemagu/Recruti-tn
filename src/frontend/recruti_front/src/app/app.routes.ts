import { Routes } from '@angular/router';
import { RecruiterOffersComponent } from './components/recruiter-offers/recruiter-offers.component';
import { CandidateOffersComponent } from './components/candidate-offers/candidate-offers.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'recruteur/offres' },
	{ path: 'recruteur/offres', component: RecruiterOffersComponent },
	{ path: 'candidat/offres', component: CandidateOffersComponent },
	{ path: '**', redirectTo: 'recruteur/offres' }
];
