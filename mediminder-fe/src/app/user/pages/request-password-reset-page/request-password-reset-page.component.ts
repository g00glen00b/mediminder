import {Component, inject, model} from '@angular/core';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {FormsModule} from '@angular/forms';
import {UserService} from '../../services/user.service';
import {RouterLink} from '@angular/router';
import {ErrorResponse} from '../../../shared/models/error-response';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'mediminder-request-password-reset-page',
  imports: [
    CentralCardComponent,
    FormsModule,
    AlertComponent,
    MatButton,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatAnchor,
    RouterLink
  ],
  templateUrl: './request-password-reset-page.component.html',
  styleUrl: './request-password-reset-page.component.scss'
})
export class RequestPasswordResetPageComponent {
  private readonly userService = inject(UserService);
  email = model('');
  error?: ErrorResponse;
  successRequestPasswordReset = false;

  submit() {
    this.successRequestPasswordReset = false;
    this.userService.requestResetCredentials(this.email()).subscribe({
      next: () => this.successRequestPasswordReset = true,
      error: response => this.error = response.error,
    });
  }
}
