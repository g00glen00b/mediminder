import {Component, output} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { MatListModule } from '@angular/material/list';

@Component({
    selector: 'mediminder-sidenav',
    templateUrl: './sidenav.component.html',
    styleUrls: ['./sidenav.component.scss'],
    imports: [
        MatListModule,
        RouterLink,
        MatIconModule
    ]
})
export class SidenavComponent {
  logout = output<void>();
}
