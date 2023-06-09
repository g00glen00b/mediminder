import {Component, EventEmitter, Output} from '@angular/core';

@Component({
  selector: 'mediminder-sidenav-navbar',
  templateUrl: './sidenav-navbar.component.html',
  styleUrls: ['./sidenav-navbar.component.scss']
})
export class SidenavNavbarComponent {
  @Output()
  closeToggle: EventEmitter<void> = new EventEmitter<void>();
}
