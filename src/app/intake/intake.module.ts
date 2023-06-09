import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {IntakeOverviewComponent} from './components/intake-overview/intake-overview.component';
import {IntakeListComponent} from './components/intake-list/intake-list.component';
import {SharedModule} from "../shared/shared.module";
import {MatButtonModule} from "@angular/material/button";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {IntakeDialogComponent} from './components/intake-dialog/intake-dialog.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatDialogModule} from "@angular/material/dialog";
import {ScheduleModule} from "../schedule/schedule.module";
import {RouterLink} from "@angular/router";


@NgModule({
  declarations: [
    IntakeOverviewComponent,
    IntakeListComponent,
    IntakeDialogComponent
  ],
  exports: [
    IntakeOverviewComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatToolbarModule,
    MatDialogModule,
    ScheduleModule,
    RouterLink
  ]
})
export class IntakeModule { }
