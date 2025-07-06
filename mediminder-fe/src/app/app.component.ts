import {Component, inject, OnInit} from '@angular/core';
import {MatSidenavModule} from '@angular/material/sidenav';
import {NavbarComponent} from './shared/components/navbar/navbar.component';
import {BottomNavComponent} from './shared/components/bottom-nav/bottom-nav.component';
import {RouterOutlet} from '@angular/router';
import {MatIconRegistry} from '@angular/material/icon';

@Component({
  selector: 'mediminder-root',
  imports: [MatSidenavModule, NavbarComponent, BottomNavComponent, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private readonly iconRegistry = inject(MatIconRegistry);

  ngOnInit() {
    this.iconRegistry.setDefaultFontSetClass('material-symbols-outlined');
  }
}
