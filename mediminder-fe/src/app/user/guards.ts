import {CanActivateFn} from '@angular/router';
import {inject} from '@angular/core';
import {UserService} from './services/user.service';
import {map} from 'rxjs';
import {environment} from '../../environment/environment';

export const IsLoggedIn: CanActivateFn = () => {
  const userService = inject(UserService);
  return userService.isLoggedIn()
    .pipe(map(isLoggedIn => {
      if (isLoggedIn) return true;
      window.location.href = environment.loginHandler;
      return false;
    }));
}

export function HasAuthority(authority: string): CanActivateFn {
  return () => {
    const userService = inject(UserService);
    return userService.hasAuthority(authority);
  };
}
