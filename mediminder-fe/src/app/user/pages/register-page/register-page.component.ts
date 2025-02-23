import {Component, computed, DestroyRef, inject, model, signal} from '@angular/core';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {UserService} from '../../services/user.service';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {MatAutocomplete, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {mergeMap, throttleTime} from 'rxjs';
import {RegisterUserRequest} from '../../models/register-user-request';
import {ErrorResponse} from '../../../shared/models/error-response';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MatOption} from '@angular/material/select';
import {SamePasswordDirective} from '../../directives/same-password.directive';

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
    FormsModule,
    RouterLink,
    AlertComponent,
    SamePasswordDirective,
  ],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss'
})
export class RegisterPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly userService = inject(UserService);

  email = model('');
  password = model('');
  repeatPassword = model('');
  name = model('');
  timezone = model('');
  timezoneInputValue = signal('');
  timezones = toSignal(toObservable(this.timezoneInputValue).pipe(
    takeUntilDestroyed(this.destroyRef),
    throttleTime(300),
    mergeMap(search => this.userService.findAvailableTimezones(search || ''))
  ), {initialValue: []});
  request = computed<RegisterUserRequest>(() => ({
    email: this.email(),
    name: this.name(),
    password: this.password(),
    timezone: this.timezone(),
  }));

  error?: ErrorResponse;
  success: boolean = false;
  successVerification: boolean = false;

  submit(): void {
    this.resetState();
    this.userService.register(this.request()).subscribe({
      next: () => this.success = true,
      error: (response) => this.error = response.error,
    });
  }

  resetVerification(): void {
    this.resetState();
    const email = this.email();
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
