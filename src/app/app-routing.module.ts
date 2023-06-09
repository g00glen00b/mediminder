import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [{
  path: 'home',
  loadChildren: () => import('./home/home.module').then(module => module.HomeModule),
}, {
  path: 'cabinet',
  loadChildren: () => import('./cabinet/cabinet.module').then(module => module.CabinetModule),
}, {
  path: 'schedule',
  loadChildren: () => import('./schedule/schedule.module').then(module => module.ScheduleModule),
}, {
  path: 'medication',
  loadChildren: () => import('./medication/medication.module').then(module => module.MedicationModule),
}, {
  path: 'tools',
  loadChildren: () => import('./tools/tools.module').then(module => module.ToolsModule),
}, {
  path: '',
  redirectTo: 'home',
  pathMatch: 'full'
}];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
