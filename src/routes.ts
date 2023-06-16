import {Routes} from "@angular/router";
import {HomePageComponent} from "./app/home/pages/home-page/home-page.component";
import {CabinetOverviewPageComponent} from "./app/cabinet/pages/cabinet-overview-page/cabinet-overview-page.component";
import {
  CreateCabinetEntryPageComponent
} from "./app/cabinet/pages/create-cabinet-entry-page/create-cabinet-entry-page.component";
import {
  EditCabinetEntryPageComponent
} from "./app/cabinet/pages/edit-cabinet-entry-page/edit-cabinet-entry-page.component";
import {
  ScheduleOverviewPageComponent
} from "./app/schedule/pages/schedule-overview-page/schedule-overview-page.component";
import {CreateSchedulePageComponent} from "./app/schedule/pages/create-schedule-page/create-schedule-page.component";
import {EditSchedulePageComponent} from "./app/schedule/pages/edit-schedule-page/edit-schedule-page.component";
import {
  MedicationOverviewPageComponent
} from "./app/medication/pages/medication-overview-page/medication-overview-page.component";
import {ToolsOverviewComponent} from "./app/tools/pages/tools-overview/tools-overview.component";

export const routes: Routes = [{
  path: 'home',
  children: [
    {
      path: '',
      component: HomePageComponent,
    },
  ]
}, {
  path: 'cabinet',
  children: [
    {
      path: '',
      component: CabinetOverviewPageComponent,
    },
    {
      path: 'create',
      component: CreateCabinetEntryPageComponent,
    },
    {
      path: ':id/copy',
      component: CreateCabinetEntryPageComponent,
    },
    {
      path: ':id/edit',
      component: EditCabinetEntryPageComponent,
    }
  ]
}, {
  path: 'schedule',
  children: [
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
  ]
}, {
  path: 'medication',
  children: [
    {
      path: '',
      component: MedicationOverviewPageComponent,
    },
  ]
}, {
  path: 'tools',
  children: [
    {
      path: '',
      component: ToolsOverviewComponent,
    }
  ]
}, {
  path: '',
  redirectTo: 'home',
  pathMatch: 'full'
}];
