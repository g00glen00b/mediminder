import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CabinetOverviewPageComponent} from './pages/cabinet-overview-page/cabinet-overview-page.component';
import {CabinetRoutingModule} from "./cabinet-routing.module";
import {SharedModule} from "../shared/shared.module";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {MatListModule} from "@angular/material/list";
import {CabinetListComponent} from './components/cabinet-list/cabinet-list.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {CreateCabinetEntryPageComponent} from './pages/create-cabinet-entry-page/create-cabinet-entry-page.component';
import {CabinetEntryFormComponent} from './components/cabinet-entry-form/cabinet-entry-form.component';
import {MatInputModule} from "@angular/material/input";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {ReactiveFormsModule} from "@angular/forms";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {EditCabinetEntryPageComponent} from './pages/edit-cabinet-entry-page/edit-cabinet-entry-page.component';
import { CabinetEntryDialogComponent } from './components/cabinet-entry-dialog/cabinet-entry-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatToolbarModule} from "@angular/material/toolbar";
import {ScheduleModule} from "../schedule/schedule.module";


@NgModule({
  declarations: [
    CabinetOverviewPageComponent,
    CabinetListComponent,
    CreateCabinetEntryPageComponent,
    CabinetEntryFormComponent,
    EditCabinetEntryPageComponent,
    CabinetEntryDialogComponent,
  ],
  imports: [
    CommonModule,
    CabinetRoutingModule,
    SharedModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatListModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatDialogModule,
    MatToolbarModule,
    ScheduleModule,
  ]
})
export class CabinetModule { }
