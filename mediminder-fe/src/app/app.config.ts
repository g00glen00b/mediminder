import {ApplicationConfig} from '@angular/core';
import {provideRouter, withComponentInputBinding, withRouterConfig} from '@angular/router';

import {routes} from './app.routes';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {provideToastr} from 'ngx-toastr';
import {provideHttpClient, withInterceptors, withInterceptorsFromDi, withXsrfConfiguration} from '@angular/common/http';
import {provideDateFnsAdapter} from '@angular/material-date-fns-adapter';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {enUS} from 'date-fns/locale';
import {provideServiceWorker} from '@angular/service-worker';
import {authHttpInterceptorFn, provideAuth0} from '@auth0/auth0-angular';
import {environment} from '../environment/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding(), withRouterConfig({
      onSameUrlNavigation: 'reload'
    })),
    provideAuth0({
      domain: environment.auth0Domain,
      clientId: environment.auth0ClientId,
      authorizationParams: {
        redirect_uri: window.location.origin,
        audience: environment.audience,
      },
      httpInterceptor: {
        allowedList: [
          {
            uri: `${environment.apiUrl}/*`,
            tokenOptions: {
              authorizationParams: {
                audience: environment.audience,
              }
            }
          }
        ]
      }
    }),
    provideAnimationsAsync(),
    provideToastr({
      timeOut: 10000,
      positionClass: 'toast-bottom-full-width',
      preventDuplicates: true,
    }),
    provideDateFnsAdapter(),
    provideHttpClient(withInterceptors([authHttpInterceptorFn])),
    {
      provide: MAT_DATE_LOCALE,
      useValue: enUS,
    },
    provideServiceWorker('ngsw-worker.js', {
      registrationStrategy: 'registerWhenStable:30000'
    }),
  ]
};
