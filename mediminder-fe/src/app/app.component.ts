import {Component, inject} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {MatSidenavModule} from '@angular/material/sidenav';
import {NavbarComponent} from './shared/components/navbar/navbar.component';
import {UserService} from './user/services/user.service';
import {toSignal} from '@angular/core/rxjs-interop';
import {BottomNavComponent} from './shared/components/bottom-nav/bottom-nav.component';

@Component({
    selector: 'mediminder-root',
  imports: [RouterOutlet, MatSidenavModule, NavbarComponent, BottomNavComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly userService = inject(UserService);
  isLoggedIn = toSignal(this.userService.isLoggedIn(), {initialValue: false});
}
