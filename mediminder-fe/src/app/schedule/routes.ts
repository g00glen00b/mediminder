import {Route} from '@angular/router';
import {CreateSchedulePageComponent} from './pages/create-schedule-page/create-schedule-page.component';
import {EditSchedulePageComponent} from './pages/edit-schedule-page/edit-schedule-page.component';
import {IsAuthenticated} from '../user/guards';

export default [
  {
    path: 'create',
    component: CreateSchedulePageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id/duplicate',
    component: CreateSchedulePageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id/edit',
    component: EditSchedulePageComponent,
    canActivate: [IsAuthenticated()],
  }
] as Route[];
