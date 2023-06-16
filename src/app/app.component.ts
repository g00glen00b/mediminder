import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from "@angular/router";
import {filter} from "rxjs";
import {MatDrawer, MatSidenavModule} from "@angular/material/sidenav";
import {SidenavNavbarComponent} from "./shared/components/sidenav-navbar/sidenav-navbar.component";
import {NavbarComponent} from "./shared/components/navbar/navbar.component";
import {SidenavComponent} from "./shared/components/sidenav/sidenav.component";

@Component({
  selector: 'mediminder-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [
    MatSidenavModule,
    RouterOutlet,
    SidenavNavbarComponent,
    NavbarComponent,
    SidenavComponent
  ]
})
export class AppComponent implements OnInit {
  @ViewChild(MatDrawer)
  drawer!: MatDrawer;
  private router = inject(Router);

  ngOnInit(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.drawer.toggle(false));
  }
}
