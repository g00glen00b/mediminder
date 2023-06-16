import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ScheduleOverviewPageComponent} from './pages/schedule-overview-page/schedule-overview-page.component';
import {ScheduleRoutingModule} from "./schedule-routing.module";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {SharedModule} from "../shared/shared.module";
import {CreateSchedulePageComponent} from './pages/create-schedule-page/create-schedule-page.component';
import {ScheduleFormComponent} from './components/schedule-form/schedule-form.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {ScheduleListComponent} from './components/schedule-list/schedule-list.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatListModule} from "@angular/material/list";
import {ScheduleRecurrenceTypePipe} from './pipes/schedule-recurrence-type.pipe';
import {EditSchedulePageComponent} from './pages/edit-schedule-page/edit-schedule-page.component';
import { ScheduleDialogComponent } from './components/schedule-dialog/schedule-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatToolbarModule} from "@angular/material/toolbar";


@NgModule({
    exports: [
        ScheduleRecurrenceTypePipe
    ],
    imports: [
        CommonModule,
        ScheduleRoutingModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        SharedModule,
        FormsModule,
        MatAutocompleteModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatInputModule,
        MatOptionModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatCheckboxModule,
        MatListModule,
        MatDialogModule,
        MatToolbarModule,
        ScheduleOverviewPageComponent,
        CreateSchedulePageComponent,
        ScheduleFormComponent,
        ScheduleListComponent,
        ScheduleRecurrenceTypePipe,
        EditSchedulePageComponent,
        ScheduleDialogComponent,
    ]
})
export class ScheduleModule { }
