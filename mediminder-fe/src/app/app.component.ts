import {Component} from '@angular/core';
import {MatSidenavModule} from '@angular/material/sidenav';
import {NavbarComponent} from './shared/components/navbar/navbar.component';
import {BottomNavComponent} from './shared/components/bottom-nav/bottom-nav.component';
import {RouterOutlet} from '@angular/router';

@Component({
    selector: 'mediminder-root',
  imports: [MatSidenavModule, NavbarComponent, BottomNavComponent, RouterOutlet],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
}
