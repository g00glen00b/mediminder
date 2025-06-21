import {Route} from '@angular/router';
import {PlannerOverviewPageComponent} from './pages/planner-overview-page/planner-overview-page.component';
import {IsAuthenticated} from '../user/guards';

export default [
  {
    path: '',
    component: PlannerOverviewPageComponent,
    canActivate: [IsAuthenticated()],
  },
] as Route[];
