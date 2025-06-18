import {HasAuthority, IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {AssistantOverviewPageComponent} from './pages/assistant-overview-page/assistant-overview-page.component';

export default [
  {
    path: '',
    component: AssistantOverviewPageComponent,
    canActivate: [IsLoggedIn, HasAuthority('Assistant')],
  },
] as Route[];
