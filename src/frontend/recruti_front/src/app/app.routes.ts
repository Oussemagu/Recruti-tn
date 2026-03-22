import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';

export const routes: Routes = [
	{
		path: '',
		component: LayoutComponent,
		children: [
			{ path: '', pathMatch: 'full', redirectTo: 'auth/login' },
			{
				path: 'auth',
				loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES)
			},
			{
				path: 'offers',
				loadChildren: () => import('./Offers/offers-routing.module').then((m) => m.OFFERS_ROUTES)
			},
			{ path: '**', redirectTo: 'auth/login' }
		]
	}
];
