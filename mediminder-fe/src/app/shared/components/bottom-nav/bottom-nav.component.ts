import {Component, inject} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {UserService} from '../../../user/services/user.service';
import {toSignal} from '@angular/core/rxjs-interop';

@Component({
  selector: 'mediminder-bottom-nav',
  imports: [
    MatIcon,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './bottom-nav.component.html',
  styleUrl: './bottom-nav.component.scss'
})
export class BottomNavComponent {
  private readonly userService = inject(UserService);
  showAssistant = toSignal(this.userService.hasAuthority('Assistant'), {initialValue: false});
}
