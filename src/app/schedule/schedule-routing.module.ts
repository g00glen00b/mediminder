import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ScheduleOverviewPageComponent} from "./pages/schedule-overview-page/schedule-overview-page.component";
import {CreateSchedulePageComponent} from "./pages/create-schedule-page/create-schedule-page.component";
import {EditSchedulePageComponent} from "./pages/edit-schedule-page/edit-schedule-page.component";

const routes: Routes = [
  {
    path: '',
    component: ScheduleOverviewPageComponent,
  },
  {
    path: 'create',
    component: CreateSchedulePageComponent,
  },
  {
    path: ':id/edit',
    component: EditSchedulePageComponent,
  },
  {
    path: ':id/copy',
    component: CreateSchedulePageComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ScheduleRoutingModule { }
