import {IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {CreateSchedulePageComponent} from './pages/create-schedule-page/create-schedule-page.component';
import {EditSchedulePageComponent} from './pages/edit-schedule-page/edit-schedule-page.component';

export default [
  {
    path: 'create',
    component: CreateSchedulePageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/duplicate',
    component: CreateSchedulePageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/edit',
    component: EditSchedulePageComponent,
    canActivate: [IsLoggedIn],
  }
] as Route[];
