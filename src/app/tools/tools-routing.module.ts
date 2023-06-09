import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ToolsOverviewComponent} from "./pages/tools-overview/tools-overview.component";

const routes: Routes = [
  {
    path: '',
    component: ToolsOverviewComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ToolsRoutingModule { }
