import {Component, inject} from '@angular/core';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {UserService} from '../../services/user.service';
import {toSignal} from '@angular/core/rxjs-interop';
import {MatAutocomplete, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {mergeMap, startWith, throttleTime} from 'rxjs';
import {RegisterUserRequest} from '../../models/register-user-request';
import {ErrorResponse} from '../../../shared/models/error-response';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MatOption} from '@angular/material/select';
import {samePassword} from '../../validators';

@Component({
  selector: 'mediminder-register-page',
  standalone: true,
  imports: [
    CentralCardComponent,
    MatAnchor,
    MatButton,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatAutocomplete,
    MatAutocompleteTrigger,
    MatOption,
    ReactiveFormsModule,
    RouterLink,
    AlertComponent,
  ],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss'
})
export class RegisterPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly userService = inject(UserService);
  form = this.formBuilder.group({
    email: new FormControl('', [Validators.required, Validators.email, Validators.maxLength(128)]),
    passwords: this.formBuilder.group({
      password: new FormControl('', [Validators.required]),
      repeatPassword: new FormControl('', [Validators.required]),
    }, {validators: [samePassword('password', 'repeatPassword')]}),
    name: new FormControl('', Validators.maxLength(128)),
    timezone: new FormControl('', Validators.maxLength(64)),
  });
  error?: ErrorResponse;
  success: boolean = false;
  successVerification: boolean = false;
  timezones = toSignal(this.form.get('timezone')!.valueChanges.pipe(
    startWith(''),
    throttleTime(300),
    mergeMap(search => this.userService.findAvailableTimezones(search || ''))
  ), {initialValue: []});

  submit(): void {
    this.resetState();

    const request: RegisterUserRequest = {
      email: this.form.get('email')!.value!,
      password: this.form.get('passwords')!.get('password')!.value!,
      name: this.form.get('name')!.value || undefined,
      timezone: this.form.get('timezone')!.value || undefined,
    };
    this.userService.register(request).subscribe({
      next: () => this.success = true,
      error: (response) => this.error = response.error,
    });
  }

  resetVerification(): void {
    this.resetState();
    const email = this.form.get('email')!.value!;
    this.userService.resetVerification(email).subscribe({
      next: () => this.successVerification = true,
      error: (response) => this.error = response.error,
    })
  }

  private resetState(): void {
    this.success = false;
    this.successVerification = false;
    this.error = undefined;
  }
}
