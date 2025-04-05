import {DestroyRef, inject, Injectable} from '@angular/core';
import {BehaviorSubject, filter} from 'rxjs';
import {NavbarState} from '../models/navbar-state';
import {NavigationStart, Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Injectable({
  providedIn: 'root'
})
export class NavbarService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  readonly state = new BehaviorSubject<NavbarState>({});


  constructor() {
    this.router.events.pipe(
      takeUntilDestroyed(this.destroyRef),
      filter(event => event instanceof NavigationStart)
    ).subscribe(() => this.state.next({}));
  }

  setTitle(title: string) {
    this.state.next({...this.state.getValue(), title});
  }

  enableBackButton(backButtonRoute: any[]) {
    this.state.next({...this.state.getValue(), backButtonRoute});
  }
}
