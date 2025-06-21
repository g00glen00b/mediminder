import {Route} from '@angular/router';
import {MedicationOverviewPageComponent} from './pages/medication-overview-page/medication-overview-page.component';
import {CreateMedicationPageComponent} from './pages/create-medication-page/create-medication-page.component';
import {EditMedicationPageComponent} from './pages/edit-medication-page/edit-medication-page.component';
import {MedicationDetailPageComponent} from './pages/medication-detail-page/medication-detail-page.component';
import {IsAuthenticated} from '../user/guards';

export default [
  {
    path: '',
    component: MedicationOverviewPageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: 'create',
    component: CreateMedicationPageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id',
    component: MedicationDetailPageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id/duplicate',
    component: CreateMedicationPageComponent,
    canActivate: [IsAuthenticated()],
  },
  {
    path: ':id/edit',
    component: EditMedicationPageComponent,
    canActivate: [IsAuthenticated()],
  }
] as Route[];
