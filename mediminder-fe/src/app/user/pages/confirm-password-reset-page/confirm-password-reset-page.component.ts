import {Component, inject} from '@angular/core';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {UserService} from '../../services/user.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {samePassword} from '../../validators';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {filter, map, mergeMap} from 'rxjs';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';

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
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './confirm-password-reset-page.component.html',
  styleUrl: './confirm-password-reset-page.component.scss'
})
export class ConfirmPasswordResetPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  form = this.formBuilder.group({
    password: new FormControl('', [Validators.required]),
    repeatPassword: new FormControl('', [Validators.required]),
  }, {validators: [samePassword('password', 'repeatPassword')]});
  error?: ErrorResponse;
  successPasswordReset = false;

  submit() {
    this.successPasswordReset = false;
    const newPassword = this.form.get('password')!.value!;
    this.route.queryParamMap.pipe(
      map(params => params.get('code')),
      filter(passwordResetCode => passwordResetCode != null),
      map(passwordResetCode => passwordResetCode as string),
      map(passwordResetCode => ({newPassword, passwordResetCode})),
      mergeMap(request => this.userService.confirmResetCredentials(request)),
    ).subscribe({
      next: () => this.successPasswordReset = true,
      error: response => this.error = response.error,
    });
  }
}
