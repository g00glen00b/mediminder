import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {User} from '../models/user';
import {environment} from '../../../environment/environment';
import {UpdateUserRequest} from '../models/update-user-request';
import {AuthService} from '@auth0/auth0-angular';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly httpClient = inject(HttpClient);
  private readonly authService = inject(AuthService);

  hasAuthority(authority: string) {
    return this.authService.idTokenClaims$.pipe(
      map(claims => {
        const authorities: string[] = claims ? claims[environment.authoritiesClaim] : [];
        return authorities.includes(authority);
      })
    );
  }

  findCurrentUser(): Observable<User> {
    return this.httpClient.get<User>(`${environment.apiUrl}/user/current`);
  }

  findAvailableTimezones(search: string | undefined = ''): Observable<string[]> {
    const params = new HttpParams().set('search', search);
    return this.httpClient.get<string[]>(`${environment.apiUrl}/user/timezone`, {params});
  }

  update(request: UpdateUserRequest): Observable<User> {
    return this.httpClient.put<User>(`${environment.apiUrl}/user`, request);
  }

  delete(): Observable<void> {
    return this.httpClient.delete<void>(`${environment.apiUrl}/user`, {withCredentials: true});
  }
}
