import {Component, computed, inject, model} from '@angular/core';
import {CentralCardComponent} from '../../../shared/components/central-card/central-card.component';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
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
    FormsModule,
    MatButton,
    RouterLink,
    AlertComponent,
    MatAnchor
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  email = model('');
  password = model('');
  request = computed<Credentials>(() => ({
    email: this.email(),
    password: this.password()
  }))
  error?: ErrorResponse;
  successVerification: boolean = false;

  submit(): void {
    this.resetState();
    this.userService.login(this.request()).subscribe({
      next: () => this.router.navigate([`/home`]),
      error: response => this.error = response.error,
    });
  }

  resetVerification(): void {
    this.resetState();
    this.userService.resetVerification(this.email()).subscribe({
      next: () => this.successVerification = true,
      error: response => this.error = response.error,
    })
  }

  private resetState(): void {
    this.successVerification = false;
    this.error = undefined;
  }
}
