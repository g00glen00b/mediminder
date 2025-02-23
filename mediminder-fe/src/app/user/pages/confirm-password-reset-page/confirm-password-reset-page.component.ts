import {Component, computed, inject, model} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {UserService} from '../../services/user.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {filter, map} from 'rxjs';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {SamePasswordDirective} from '../../directives/same-password.directive';
import {toSignal} from '@angular/core/rxjs-interop';
import {ResetCredentialsRequest} from '../../models/reset-credentials-request';

@Component({
  selector: 'mediminder-confirm-password-reset-page',
  imports: [
    AlertComponent,
    CentralCardComponent,
    MatAnchor,
    MatButton,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    FormsModule,
    RouterLink,
    SamePasswordDirective,
  ],
  templateUrl: './confirm-password-reset-page.component.html',
  styleUrl: './confirm-password-reset-page.component.scss'
})
export class ConfirmPasswordResetPageComponent {
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  password = model('');
  repeatPassword = model('');
  passwordResetCode = toSignal(this.route.queryParamMap.pipe(
    map(params => params.get('code')),
    filter(passwordResetCode => passwordResetCode != null)));
  request = computed<ResetCredentialsRequest | undefined>(() => {
    if (!this.passwordResetCode()) return undefined;
    return {
      newPassword: this.password(),
      passwordResetCode: this.passwordResetCode()!,
    };
  });

  error?: ErrorResponse;
  successPasswordReset = false;

  submit() {
    this.successPasswordReset = false;
    this.userService
      .confirmResetCredentials(this.request()!)
      .subscribe({
        next: () => this.successPasswordReset = true,
        error: response => this.error = response.error,
      });
  }
}
