import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertComponent } from './components/alert/alert.component';
import { AlertListComponent } from './components/alert-list/alert-list.component';
import {MatIconModule} from "@angular/material/icon";



@NgModule({
  declarations: [
    AlertComponent,
    AlertListComponent
  ],
  exports: [
    AlertComponent,
    AlertListComponent
  ],
  imports: [
    CommonModule,
    MatIconModule
  ]
})
export class AlertModule { }
