import { Routes } from '@angular/router';
import { ProfileComponent } from './features/user/profile/profile.component';
import { EditProfileComponent } from './features/user/edit-profile/edit-profile.component';
import { VerifyDeleteAccountComponent } from './features/user/delete-account/verify-delete-account.component';
import { ConfirmDeleteAccountComponent } from './features/user/delete-account/confirm-delete-account.component';

export const routes: Routes = [
  { path: 'profile', component: ProfileComponent },
  { path: 'edit-profile', component: EditProfileComponent },
  { path: 'delete-account', component: VerifyDeleteAccountComponent },
  { path: 'delete-account/confirm', component: ConfirmDeleteAccountComponent },
  { path: '', redirectTo: '/profile', pathMatch: 'full' }
];
