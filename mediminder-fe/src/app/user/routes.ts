import {LoginPageComponent} from './pages/login-page/login-page.component';
import {Route} from '@angular/router';
import {RegisterPageComponent} from './pages/register-page/register-page.component';
import {IsLoggedIn, IsNotLoggedIn} from './guards';
import {VerifyPageComponent} from './pages/verify-page/verify-page.component';
import {EditProfilePageComponent} from './pages/edit-profile-page/edit-profile-page.component';
import {
  RequestPasswordResetPageComponent
} from './pages/request-password-reset-page/request-password-reset-page.component';
import {
  ConfirmPasswordResetPageComponent
} from './pages/confirm-password-reset-page/confirm-password-reset-page.component';

export default [
  {
    path: 'login',
    component: LoginPageComponent,
    canActivate: [IsNotLoggedIn],
  },
  {
    path: 'register',
    component: RegisterPageComponent,
    canActivate: [IsNotLoggedIn],
  },
  {
    path: 'verify',
    component: VerifyPageComponent,
    canActivate: [IsNotLoggedIn],
  },
  {
    path: 'profile',
    component: EditProfilePageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: 'password-reset',
    component: RequestPasswordResetPageComponent,
    canActivate: [IsNotLoggedIn],
  },
  {
    path: 'confirm-password-reset',
    component: ConfirmPasswordResetPageComponent,
    canActivate: [IsNotLoggedIn],
  }
] as Route[];
