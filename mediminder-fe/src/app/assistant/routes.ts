import {HasAuthority, IsAuthenticated} from '../user/guards';
import {Route} from '@angular/router';
import {AssistantOverviewPageComponent} from './pages/assistant-overview-page/assistant-overview-page.component';

export default [
  {
    path: '',
    component: AssistantOverviewPageComponent,
    canActivate: [IsAuthenticated(), HasAuthority('Assistant')],
  },
] as Route[];
