import {IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {PlannerOverviewPageComponent} from './pages/planner-overview-page/planner-overview-page.component';

export default [
  {
    path: '',
    component: PlannerOverviewPageComponent,
    canActivate: [IsLoggedIn],
  },
] as Route[];
