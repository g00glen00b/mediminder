import {AppComponent} from './app/app.component';
import {importProvidersFrom, isDevMode} from '@angular/core';
import {provideServiceWorker} from '@angular/service-worker';
import {provideToastr} from 'ngx-toastr';
import {MatDateFnsModule} from '@angular/material-date-fns-adapter';
import {provideAnimations} from '@angular/platform-browser/animations';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {enUS} from 'date-fns/locale';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {provideRouter, withComponentInputBinding} from "@angular/router";
import {NgxIndexedDBModule} from "ngx-indexed-db";
import {dbConfig} from "./db";
import {routes} from "./routes";
import {MatDialogModule} from "@angular/material/dialog";


bootstrapApplication(AppComponent, {
    providers: [
      importProvidersFrom(BrowserModule),
      importProvidersFrom(NgxIndexedDBModule.forRoot(dbConfig)),
      importProvidersFrom(MatDateFnsModule),
      importProvidersFrom(MatDialogModule),
      {
        provide: MAT_DATE_LOCALE,
        useValue: enUS,
      },
      provideRouter(routes, withComponentInputBinding()),
      provideServiceWorker('ngsw-worker.js', {
        enabled: !isDevMode(),
        // Register the ServiceWorker as soon as the application is stable
        // or after 30 seconds (whichever comes first).
        registrationStrategy: 'registerWhenStable:30000'
      }),
      provideToastr({
        timeOut: 5000,
        positionClass: 'toast-bottom-full-width',
        preventDuplicates: true,
      }),
      provideAnimations()
    ]
})
  .catch(err => console.error(err));
