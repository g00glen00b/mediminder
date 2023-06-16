import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';


import { AppComponent } from './app/app.component';
import { isDevMode, importProvidersFrom } from '@angular/core';
import { ServiceWorkerModule } from '@angular/service-worker';
import { ToastrModule } from 'ngx-toastr';
import { MatDateFnsModule } from '@angular/material-date-fns-adapter';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AppDbModule } from './app/app-db.module';
import { AppRoutingModule } from './app/app-routing.module';
import { BrowserModule, bootstrapApplication } from '@angular/platform-browser';
import { enUS } from 'date-fns/locale';
import { MAT_DATE_LOCALE } from '@angular/material/core';


bootstrapApplication(AppComponent, {
    providers: [
      importProvidersFrom(
        BrowserModule,
        AppRoutingModule,
        AppDbModule,
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
        })
      ), {
        provide: MAT_DATE_LOCALE,
        useValue: enUS,
      },
      provideAnimations()
    ]
})
  .catch(err => console.error(err));
