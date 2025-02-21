import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {UserService} from '../../services/user.service';
import {map, mergeMap} from 'rxjs';
import {User} from '../../models/user';
import {ErrorResponse} from '../../../shared/models/error-response';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MatAnchor} from '@angular/material/button';

@Component({
  selector: 'mediminder-verify-page',
  imports: [
    CentralCardComponent,
    AlertComponent,
    MatAnchor,
    RouterLink,
  ],
  templateUrl: './verify-page.component.html',
  styleUrl: './verify-page.component.scss'
})
export class VerifyPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);
  user?: User;
  error?: ErrorResponse;

  ngOnInit() {
    this.route.queryParamMap.pipe(
      map(params => params.get('code')),
      mergeMap(code => this.userService.verify(code!)))
      .subscribe({
        next: user => this.user = user,
        error: response => this.error = response.error,
      });
  }

}
