import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {UserService} from './services/user.service';
import {map} from 'rxjs';

export const IsLoggedIn: CanActivateFn = () => {
  const router = inject(Router);
  const userService = inject(UserService);
  return userService.isLoggedIn()
    .pipe(map(isLoggedIn => {
      if (isLoggedIn) return true;
      return router.createUrlTree(['/user/login']);
    }));
}

export const IsNotLoggedIn: CanActivateFn = () => {
  const router = inject(Router);
  const userService = inject(UserService);
  return userService
    .isLoggedIn()
    .pipe(map(isLoggedIn => {
      if (!isLoggedIn) return true;
      return router.createUrlTree(['/home']);
    }));
}
