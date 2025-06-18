import {Route} from '@angular/router';
import {IsLoggedIn} from './guards';
import {EditProfilePageComponent} from './pages/edit-profile-page/edit-profile-page.component';

export default [
  {
    path: 'profile',
    component: EditProfilePageComponent,
    canActivate: [IsLoggedIn],
  }
] as Route[];
