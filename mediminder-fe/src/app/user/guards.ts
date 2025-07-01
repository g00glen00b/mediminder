import {ActivatedRouteSnapshot, CanActivateFn, RouterStateSnapshot} from '@angular/router';
import {inject} from '@angular/core';
import {UserService} from './services/user.service';
import {AuthService} from '@auth0/auth0-angular';
import {tap} from 'rxjs';

export function IsAuthenticated(): CanActivateFn {
  return (_: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthService);
    return authService.isAuthenticated$.pipe(
      tap(isAuthenticated => {
        if (!isAuthenticated) {
          authService.loginWithRedirect({
            appState: {
              target: state.url
            }
          });
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
