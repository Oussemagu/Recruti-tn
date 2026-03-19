import { Routes } from '@angular/router';
import { ProfileComponent } from './features/user/profile/profile.component';
import { EditProfileComponent } from './features/user/edit-profile/edit-profile.component';
import { DeleteAccountComponent } from './features/user/delete-account/delete-account.component';

export const routes: Routes = [
  { path: 'profile', component: ProfileComponent },
  { path: 'edit-profile', component: EditProfileComponent },
  { path: 'delete-account', component: DeleteAccountComponent },
  { path: '', redirectTo: '/profile', pathMatch: 'full' }
];
