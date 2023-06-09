import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MedicationOverviewPageComponent} from "./pages/medication-overview-page/medication-overview-page.component";

const routes: Routes = [
  {
    path: '',
    component: MedicationOverviewPageComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MedicationRoutingModule { }
