import {HttpHandlerFn, HttpRequest, HttpResponse} from '@angular/common/http';
import {inject} from '@angular/core';
import {HttpRequestService} from '../services/http-request.service';
import {tap} from 'rxjs';
import {finalize} from 'rxjs/operators';

export function progressInterceptor(request: HttpRequest<unknown>, next: HttpHandlerFn) {
  const httpRequestService = inject(HttpRequestService);
  httpRequestService.add(request);

  return next(request)
    .pipe(
      tap({
        next: (event) => {
          if (event instanceof HttpResponse) {
            httpRequestService.remove(request);
          }
        },
        error: () => {
          httpRequestService.remove(request);
        }
      }),
      finalize(() => {
        httpRequestService.remove(request);
      })
    );
}
