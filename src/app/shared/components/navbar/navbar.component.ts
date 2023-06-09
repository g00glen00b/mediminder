import {Component, EventEmitter, Output} from '@angular/core';

@Component({
  selector: 'mediminder-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  @Output()
  menuToggle: EventEmitter<void> = new EventEmitter<void>();
}
