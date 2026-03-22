import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
	selector: 'app-layout',
	standalone: true,
	imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
	template: `
		<div class="min-h-screen bg-slate-50">
			<header class="border-b border-slate-200 bg-white">
				<div class="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
					<h1 class="text-lg font-semibold text-slate-900">Recruti</h1>

					<nav class="flex items-center gap-2">
						<a
							routerLink="/auth/login"
							routerLinkActive="bg-indigo-100 text-indigo-700"
							class="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
						>
							Connexion
						</a>
						<a
							routerLink="/offers/candidate"
							routerLinkActive="bg-indigo-100 text-indigo-700"
							class="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
						>
							Candidat
						</a>
						<a
							routerLink="/offers/recruiter"
							routerLinkActive="bg-indigo-100 text-indigo-700"
							class="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
						>
							Recruteur
						</a>
						<button
							type="button"
							(click)="logout()"
							class="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
						>
							Déconnexion
						</button>
					</nav>
				</div>
			</header>

			<main>
				<router-outlet></router-outlet>
			</main>
		</div>
	`
})
export class LayoutComponent {
	constructor(private readonly router: Router) {}

	logout(): void {
		localStorage.removeItem('userRole');
		localStorage.removeItem('userId');
		this.router.navigate(['/auth/login']);
	}
}
