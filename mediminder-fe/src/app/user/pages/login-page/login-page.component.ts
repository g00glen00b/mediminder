import {Component, inject} from '@angular/core';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatAnchor, MatButton} from '@angular/material/button';
import {Router, RouterLink} from '@angular/router';
import {UserService} from '../../services/user.service';
import {Credentials} from '../../models/credentials';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ErrorResponse} from '../../../shared/models/error-response';

@Component({
  selector: 'mediminder-login-page',
  imports: [
    CentralCardComponent,
    MatFormField,
    MatInput,
    MatLabel,
    MatHint,
    MatError,
    ReactiveFormsModule,
    MatButton,
    RouterLink,
    AlertComponent,
    MatAnchor
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  form = this.formBuilder.group({
    email: new FormControl('', [Validators.required, Validators.email, Validators.maxLength(128)]),
    password: new FormControl('', [Validators.required]),
  });
  error?: ErrorResponse;
  successVerification: boolean = false;

  submit(): void {
    this.resetState();
    const request: Credentials = {
      email: this.form.get('email')!.value!,
      password: this.form.get('password')!.value!,
    };
    this.userService.login(request).subscribe({
      next: () => this.router.navigate([`/home`]),
      error: response => this.error = response.error,
    });
  }

  resetVerification(): void {
    this.resetState();
    const email = this.form.get('email')!.value!;
    this.userService.resetVerification(email).subscribe({
      next: () => this.successVerification = true,
      error: response => this.error = response.error,
    })
  }

  private resetState(): void {
    this.successVerification = false;
    this.error = undefined;
  }
}
