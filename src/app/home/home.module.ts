import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomePageComponent } from './pages/home-page/home-page.component';
import {HomeRoutingModule} from "./home-routing.module";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {SharedModule} from "../shared/shared.module";
import {IntakeModule} from "../intake/intake.module";
import {AlertModule} from "../alert/alert.module";



@NgModule({
  declarations: [
    HomePageComponent
  ],
  imports: [
    CommonModule,
    HomeRoutingModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    SharedModule,
    IntakeModule,
    AlertModule,
  ]
})
export class HomeModule { }
