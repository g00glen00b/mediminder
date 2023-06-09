import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {CabinetOverviewPageComponent} from "./pages/cabinet-overview-page/cabinet-overview-page.component";
import {CreateCabinetEntryPageComponent} from "./pages/create-cabinet-entry-page/create-cabinet-entry-page.component";
import {EditCabinetEntryPageComponent} from "./pages/edit-cabinet-entry-page/edit-cabinet-entry-page.component";

const routes: Routes = [
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
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CabinetRoutingModule { }
