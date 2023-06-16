import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MedicationOverviewPageComponent} from './pages/medication-overview-page/medication-overview-page.component';
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {RouterLink} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {MedicationRoutingModule} from "./medication-routing.module";
import {MedicationListComponent} from './components/medication-list/medication-list.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatListModule} from "@angular/material/list";
import { MedicationDialogComponent } from './components/medication-dialog/medication-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatToolbarModule} from "@angular/material/toolbar";


@NgModule({
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        RouterLink,
        SharedModule,
        MedicationRoutingModule,
        MatCheckboxModule,
        MatListModule,
        MatDialogModule,
        MatToolbarModule,
        MedicationOverviewPageComponent,
        MedicationListComponent,
        MedicationDialogComponent,
    ]
})
export class MedicationModule { }
