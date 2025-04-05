import {IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {MedicationOverviewPageComponent} from './pages/medication-overview-page/medication-overview-page.component';
import {CreateMedicationPageComponent} from './pages/create-medication-page/create-medication-page.component';
import {EditMedicationPageComponent} from './pages/edit-medication-page/edit-medication-page.component';
import {MedicationDetailPageComponent} from './pages/medication-detail-page/medication-detail-page.component';

export default [
  {
    path: '',
    component: MedicationOverviewPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: 'create',
    component: CreateMedicationPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id',
    component: MedicationDetailPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/duplicate',
    component: CreateMedicationPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/edit',
    component: EditMedicationPageComponent,
    canActivate: [IsLoggedIn],
  }
] as Route[];
