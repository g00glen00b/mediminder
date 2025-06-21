import {Route} from '@angular/router';
import {EditProfilePageComponent} from './pages/edit-profile-page/edit-profile-page.component';
import {IsAuthenticated} from './guards';

export default [
  {
    path: 'profile',
    component: EditProfilePageComponent,
    canActivate: [IsAuthenticated()],
  }
] as Route[];
