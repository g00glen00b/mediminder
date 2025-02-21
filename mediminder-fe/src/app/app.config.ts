import {ApplicationConfig} from '@angular/core';
import {provideRouter, withComponentInputBinding, withRouterConfig} from '@angular/router';

import {routes} from './app.routes';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {provideToastr} from 'ngx-toastr';
import { provideHttpClient, withInterceptorsFromDi, withXsrfConfiguration } from '@angular/common/http';
import {provideDateFnsAdapter} from '@angular/material-date-fns-adapter';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {enUS} from 'date-fns/locale';
import {provideServiceWorker} from '@angular/service-worker';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding(), withRouterConfig({
        onSameUrlNavigation: 'reload'
    })),
    provideAnimationsAsync(),
    provideToastr({
        timeOut: 10000,
        positionClass: 'toast-bottom-full-width',
        preventDuplicates: true,
    }),
    provideDateFnsAdapter(),
    provideHttpClient(withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
    }), withInterceptorsFromDi()),
    {
        provide: MAT_DATE_LOCALE,
        useValue: enUS,
    },
    provideServiceWorker('ngsw-worker.js', {
        registrationStrategy: 'registerWhenStable:30000'
    }),
]
};
