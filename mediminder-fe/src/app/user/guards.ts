import {CanActivateFn} from '@angular/router';
import {inject} from '@angular/core';
import {UserService} from './services/user.service';
import {AuthService} from '@auth0/auth0-angular';
import {map, tap} from 'rxjs';
import {environment} from '../../environment/environment';

export function IsAuthenticated(): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    return authService.isAuthenticated$.pipe(
      tap(isAuthenticated => {
        if (!isAuthenticated) {
          authService.loginWithRedirect();
        }
      })
    );
  }
}

export function HasAuthority(authority: string): CanActivateFn {
  return () => {
    const userService = inject(UserService);
    return userService.hasAuthority(authority);
  };
}
