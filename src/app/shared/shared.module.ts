import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './components/navbar/navbar.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import { HeroComponent } from './components/hero/hero.component';
import {RouterLink, RouterLinkActive} from "@angular/router";
import {HeroTitleDirective} from "./components/hero/hero-title.directive";
import {HeroActionsDirective} from "./components/hero/hero-actions.directive";
import {HeroDescriptionDirective} from "./components/hero/hero-description.directive";
import {MatButtonModule} from "@angular/material/button";
import { ContainerComponent } from './components/container/container.component';
import { SortButtonComponent } from './components/sort-button/sort-button.component';
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import { SidenavComponent } from './components/sidenav/sidenav.component';
import {MatListModule} from "@angular/material/list";
import { SidenavNavbarComponent } from './components/sidenav-navbar/sidenav-navbar.component';
import { ConfirmationModalComponent } from './components/confirmation-modal/confirmation-modal.component';
import {MatDialogModule} from "@angular/material/dialog";
import { DatePaginatorComponent } from './components/date-paginator/date-paginator.component';
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {MatDatepickerModule} from "@angular/material/datepicker";
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { SwipeGestureDirective } from './directives/swipe-gesture.directive';



@NgModule({
  declarations: [
    NavbarComponent,
    HeroComponent,
    HeroTitleDirective,
    HeroActionsDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    SortButtonComponent,
    SidenavComponent,
    SidenavNavbarComponent,
    ConfirmationModalComponent,
    DatePaginatorComponent,
    EmptyStateComponent,
    SwipeGestureDirective,
  ],
  exports: [
    NavbarComponent,
    HeroComponent,
    HeroTitleDirective,
    HeroActionsDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    SortButtonComponent,
    SidenavNavbarComponent,
    SidenavComponent,
    DatePaginatorComponent,
    EmptyStateComponent,
    SwipeGestureDirective,
  ],
  imports: [
    CommonModule,
    MatToolbarModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    RouterLinkActive,
    MatListModule,
    MatDialogModule,
    MatButtonToggleModule,
    MatDatepickerModule
  ]
})
export class SharedModule { }
