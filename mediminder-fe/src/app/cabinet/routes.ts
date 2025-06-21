import {Route} from '@angular/router';
import {CreateCabinetEntryPageComponent} from './pages/create-cabinet-entry-page/create-cabinet-entry-page.component';
import {EditCabinetEntryPageComponent} from './pages/edit-cabinet-entry-page/edit-cabinet-entry-page.component';
import {IsAuthenticated} from '../user/guards';

export default [
  {
    path: 'create',
    component: CreateCabinetEntryPageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id/duplicate',
    component: CreateCabinetEntryPageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id/edit',
    component: EditCabinetEntryPageComponent,
    canActivate: [IsAuthenticated()],
  }
] as Route[];
