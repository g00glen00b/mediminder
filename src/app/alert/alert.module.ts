import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertComponent } from './components/alert/alert.component';
import { AlertListComponent } from './components/alert-list/alert-list.component';
import {MatIconModule} from "@angular/material/icon";



@NgModule({
    exports: [
        AlertComponent,
        AlertListComponent
    ],
    imports: [
        CommonModule,
        MatIconModule,
        AlertComponent,
        AlertListComponent
    ]
})
export class AlertModule { }
