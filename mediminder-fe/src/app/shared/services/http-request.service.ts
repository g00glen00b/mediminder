import {Injectable} from '@angular/core';
import {HttpRequest} from '@angular/common/http';
import {BehaviorSubject, map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HttpRequestService {
  private requestsInProgressSubject: BehaviorSubject<HttpRequest<any>[]> = new BehaviorSubject<HttpRequest<any>[]>([]);

  add(request: HttpRequest<any>): void {
    this.requestsInProgressSubject.next([...this.requestsInProgressSubject.value, request]);
  }

  remove(request: HttpRequest<any>): void {
    this.requestsInProgressSubject.next(this.requestsInProgressSubject.value.filter(requestInProgress => requestInProgress !== request));
  }

  isRequestPending(): Observable<boolean> {
    return this.requestsInProgressSubject.asObservable().pipe(map((requests) => requests.length > 0));
  }
}
