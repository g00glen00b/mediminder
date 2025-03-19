import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from '@angular/common/http';
import {BehaviorSubject, catchError, filter, map, Observable, tap, throwError} from 'rxjs';
import {User} from '../models/user';
import {environment} from '../../../environment/environment';
import {EMPTY_STATE} from '../models/authentication-state';
import {Credentials} from '../models/credentials';
import {RegisterUserRequest} from '../models/register-user-request';
import {UpdateUserRequest} from '../models/update-user-request';
import {UpdateCredentialsRequest} from '../models/update-credentials-request';
import {ResetCredentialsRequest} from '../models/reset-credentials-request';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly authenticationState$$ = new BehaviorSubject(EMPTY_STATE);
  private readonly httpClient = inject(HttpClient);

  constructor() {
    this.httpClient
      .get<User>(`${environment.apiUrl}/user/current`)
      .pipe(this.updateState())
      .subscribe();
  }

  login(credentials: Credentials): Observable<User> {
    const headers: HttpHeaders = new HttpHeaders({
      Authorization: `Basic ${window.btoa(`${credentials.email}:${credentials.password}`)}`
    });
    return this.httpClient
      .get<User>(`${environment.apiUrl}/user/current`, {headers, withCredentials: true})
      .pipe(this.updateState());
  }

  isLoggedIn(): Observable<boolean> {
    return this.authenticationState$$.pipe(
      filter(({initialized}) => initialized),
      map(({user}) => user != null)
    );
  }

  register(request: RegisterUserRequest) {
    return this.httpClient.post<User>(`${environment.apiUrl}/user`, request, {withCredentials: true});
  }

  findCurrentUser(): Observable<User> {
    return this.authenticationState$$.pipe(
      filter(({initialized}) => initialized),
      filter(user => user != null),
      map(({user}) => user as User));
  }

  findAvailableTimezones(search: string | undefined = ''): Observable<string[]> {
    const params = new HttpParams().set('search', search);
    return this.httpClient.get<string[]>(`${environment.apiUrl}/user/timezone`, {params});
  }

  resetVerification(email: string): Observable<void> {
    const params = new HttpParams().set('email', email);
    return this.httpClient.post<void>(`${environment.apiUrl}/user/verify/reset`, null,{params});
  }

  verify(code: string): Observable<User> {
    const params = new HttpParams().set('code', code);
    return this.httpClient.post<User>(`${environment.apiUrl}/user/verify`, null, {params});
  }

  private updateState(logoutOnError: boolean = true) {
    return (source: Observable<User>) => source.pipe(
      tap(user => this.authenticationState$$.next({initialized: true, user})),
      catchError((error: HttpErrorResponse) => {
        if (error.status == 401 && logoutOnError) this.authenticationState$$.next({initialized: true});
        return throwError(() => error);
      }));
  }

  update(request: UpdateUserRequest): Observable<User> {
    return this.httpClient.put<User>(`${environment.apiUrl}/user`, request).pipe(
      tap(user => this.authenticationState$$.next({initialized: true, user}))
    );
  }

  updateCredentials(request: UpdateCredentialsRequest): Observable<User> {
    return this.httpClient.put<User>(`${environment.apiUrl}/user/credentials`, request).pipe(
      tap(user => this.authenticationState$$.next({initialized: true, user}))
    );
  }

  requestResetCredentials(email: string): Observable<void> {
    const params = new HttpParams().set('email', email);
    return this.httpClient.post<void>(`${environment.apiUrl}/user/credentials/reset/request`, null, {params});
  }

  confirmResetCredentials(request: ResetCredentialsRequest): Observable<void> {
    return this.httpClient.post<void>(`${environment.apiUrl}/user/credentials/reset/confirm`, request);
  }

  logout(): Observable<void> {
    return this.httpClient
      .post<void>(`${environment.apiUrl}/user/logout`, null, {withCredentials: true})
      .pipe(tap(() => this.authenticationState$$.next({initialized: true})));
  }

  delete(): Observable<void> {
    return this.httpClient
      .delete<void>(`${environment.apiUrl}/user`, {withCredentials: true})
      .pipe(tap(() => this.authenticationState$$.next({initialized: true})));
  }
}
