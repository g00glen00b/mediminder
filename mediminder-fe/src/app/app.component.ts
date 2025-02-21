import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {MatDrawer, MatSidenavModule} from '@angular/material/sidenav';
import {SidenavNavbarComponent} from './shared/components/sidenav-navbar/sidenav-navbar.component';
import {SidenavComponent} from './shared/components/sidenav/sidenav.component';
import {NavbarComponent} from './shared/components/navbar/navbar.component';
import {filter} from 'rxjs';
import {UserService} from './user/services/user.service';
import {toSignal} from '@angular/core/rxjs-interop';

@Component({
    selector: 'mediminder-root',
  imports: [RouterOutlet, MatSidenavModule, SidenavNavbarComponent, SidenavComponent, NavbarComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  @ViewChild(MatDrawer)
  drawer!: MatDrawer;
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  isLoggedIn = toSignal(this.userService.isLoggedIn(), {initialValue: false});

  ngOnInit(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.drawer.toggle(false));
  }
}
