import {NgModule, isDevMode} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatToolbarModule} from "@angular/material/toolbar";

import {MatSidenavModule} from "@angular/material/sidenav";
import {MatMenuModule} from "@angular/material/menu";
import {MatListModule} from "@angular/material/list";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {AppDbModule} from "./app-db.module";
import {MAT_DATE_LOCALE} from "@angular/material/core";
import {MatDateFnsModule} from "@angular/material-date-fns-adapter";
import {enUS} from 'date-fns/locale';
import {ToastrModule} from "ngx-toastr";
import { ServiceWorkerModule } from '@angular/service-worker';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    AppDbModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatSidenavModule,
    MatMenuModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatDateFnsModule,
    ToastrModule.forRoot({
        timeOut: 5000,
        positionClass: 'toast-bottom-full-width',
        preventDuplicates: true,
    }),
    ServiceWorkerModule.register('ngsw-worker.js', {
        enabled: !isDevMode(),
        // Register the ServiceWorker as soon as the application is stable
        // or after 30 seconds (whichever comes first).
        registrationStrategy: 'registerWhenStable:30000'
    }),
],
  providers: [{
    provide: MAT_DATE_LOCALE,
    useValue: enUS,
  }],
  bootstrap: [AppComponent]
})
export class AppModule { }
