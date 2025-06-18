import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from '@angular/common/http';
import {BehaviorSubject, catchError, filter, map, Observable, tap, throwError} from 'rxjs';
import {User} from '../models/user';
import {environment} from '../../../environment/environment';
import {EMPTY_STATE} from '../models/authentication-state';
import {UpdateUserRequest} from '../models/update-user-request';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly authenticationState$$ = new BehaviorSubject(EMPTY_STATE);
  private readonly httpClient = inject(HttpClient);

  constructor() {
    this.httpClient
      .get<User>(`./api/user/current`)
      .pipe(this.updateState())
      .subscribe();
  }

  isLoggedIn(): Observable<boolean> {
    return this.authenticationState$$.pipe(
      filter(({initialized}) => initialized),
      map(({user}) => user != null)
    );
  }

  findCurrentUser(): Observable<User> {
    return this.authenticationState$$.pipe(
      filter(({initialized}) => initialized),
      filter(user => user != null),
      map(({user}) => user as User));
  }

  hasAuthority(authority: string): Observable<boolean> {
    return this.findCurrentUser().pipe(
      map(user => user.authorities.includes(authority))
    );
  }

  findAvailableTimezones(search: string | undefined = ''): Observable<string[]> {
    const params = new HttpParams().set('search', search);
    return this.httpClient.get<string[]>(`./api/user/timezone`, {params});
  }

  private updateState(logoutOnError: boolean = true) {
    return (source: Observable<User>) => source.pipe(
      tap(user => this.authenticationState$$.next({initialized: true, user})),
      catchError((error: HttpErrorResponse) => {
        if (logoutOnError) this.authenticationState$$.next({initialized: true});
        return throwError(() => error);
      }));
  }

  update(request: UpdateUserRequest): Observable<User> {
    return this.httpClient.put<User>(`./api/user`, request).pipe(
      tap(user => this.authenticationState$$.next({initialized: true, user}))
    );
  }

  delete(): Observable<void> {
    return this.httpClient
      .delete<void>(`./api/user`, {withCredentials: true})
      .pipe(tap(() => this.authenticationState$$.next({initialized: true})));
  }

  logout(): Observable<void> {
    return this.httpClient
      .post<void>(`/logout`, null, {withCredentials: true})
      .pipe(tap(() => this.authenticationState$$.next({initialized: true})));
  }
}
