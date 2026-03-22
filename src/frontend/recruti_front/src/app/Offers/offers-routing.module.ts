import { Routes } from '@angular/router';
import { CandidateOffersComponent } from '../components/candidate-offers/candidate-offers.component';
import { RecruiterOffersComponent } from '../components/recruiter-offers/recruiter-offers.component';

export const OFFERS_ROUTES: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'candidate' },
	{ path: 'candidate', component: CandidateOffersComponent },
	{ path: 'recruiter', component: RecruiterOffersComponent }
];
