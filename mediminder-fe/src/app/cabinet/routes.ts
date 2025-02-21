import {IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {CabinetOverviewPageComponent} from './pages/cabinet-overview-page/cabinet-overview-page.component';
import {CreateCabinetEntryPageComponent} from './pages/create-cabinet-entry-page/create-cabinet-entry-page.component';
import {EditCabinetEntryPageComponent} from './pages/edit-cabinet-entry-page/edit-cabinet-entry-page.component';

export default [
  {
    path: '',
    component: CabinetOverviewPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: 'create',
    component: CreateCabinetEntryPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/duplicate',
    component: CreateCabinetEntryPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/edit',
    component: EditCabinetEntryPageComponent,
    canActivate: [IsLoggedIn],
  }
] as Route[];
