import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToolsOverviewComponent } from './pages/tools-overview/tools-overview.component';
import {ToolsRoutingModule} from "./tools-routing.module";
import {SharedModule} from "../shared/shared.module";
import { DoseCalculatorComponent } from './components/dose-calculator/dose-calculator.component';
import {MatListModule} from "@angular/material/list";
import { MissingDoseDialogComponent } from './components/missing-dose-dialog/missing-dose-dialog.component';
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";
import {MatToolbarModule} from "@angular/material/toolbar";
import {ScheduleModule} from "../schedule/schedule.module";
import { MissingDoseListComponent } from './components/missing-dose-list/missing-dose-list.component';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";



@NgModule({
  declarations: [
    ToolsOverviewComponent,
    DoseCalculatorComponent,
    MissingDoseDialogComponent,
    MissingDoseListComponent,
  ],
  imports: [
    CommonModule,
    ToolsRoutingModule,
    SharedModule,
    MatListModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatToolbarModule,
    ScheduleModule,
    MatProgressSpinnerModule,
  ]
})
export class ToolsModule { }
