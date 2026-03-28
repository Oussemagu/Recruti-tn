import { Routes } from '@angular/router';
import { ProfileViewComponent } from './profile-view/profile-view.component';
import { EditProfileComponent } from './edit-profile/edit-profile.component';

export const PROFILE_ROUTES: Routes = [
  {
    path: '',
    component: ProfileViewComponent,
    data: { title: 'My Profile' }
  },
  {
    path: 'edit',
    component: EditProfileComponent,
    data: { title: 'Edit Profile' }
  }
];
