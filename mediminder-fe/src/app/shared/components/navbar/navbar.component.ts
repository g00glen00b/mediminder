import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {NgOptimizedImage} from '@angular/common';
import {NavbarService} from '../../services/navbar.service';
import {toSignal} from '@angular/core/rxjs-interop';
import {NavbarState} from '../../models/navbar-state';

@Component({
  selector: 'mediminder-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
    NgOptimizedImage,
  ]
})
export class NavbarComponent {
  private readonly service = inject(NavbarService);
  private readonly router = inject(Router);
  state = toSignal(this.service.state.asObservable(), {initialValue: {} as NavbarState});

  navigateBack() {
    this.router.navigate(this.state().backButtonRoute!);
  }
}
