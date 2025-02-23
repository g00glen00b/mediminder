import {Component, output} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';

@Component({
    selector: 'mediminder-sidenav-navbar',
    templateUrl: './sidenav-navbar.component.html',
    styleUrls: ['./sidenav-navbar.component.scss'],
    imports: [
        MatToolbarModule,
        MatButtonModule,
        MatIconModule
    ]
})
export class SidenavNavbarComponent {
  closeToggle = output<void>();
}
