import {IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {HomePageComponent} from './pages/home-page/home-page.component';

export default [
  {
    path: '',
    component: HomePageComponent,
    canActivate: [IsLoggedIn],
  },
] as Route[];
