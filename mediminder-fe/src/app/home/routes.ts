import {Route} from '@angular/router';
import {HomePageComponent} from './pages/home-page/home-page.component';
import {IsAuthenticated} from '../user/guards';

export default [
  {
    path: '',
    component: HomePageComponent,
    canActivate: [IsAuthenticated()],
  },
] as Route[];
