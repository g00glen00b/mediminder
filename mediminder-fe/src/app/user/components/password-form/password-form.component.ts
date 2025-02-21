import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {User} from '../../models/user';
import {samePassword} from '../../validators';
import {UpdateCredentialsRequest} from '../../models/update-credentials-request';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'mediminder-password-form',
  imports: [
    ReactiveFormsModule,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatButton
  ],
  templateUrl: './password-form.component.html',
  styleUrl: './password-form.component.scss'
})
export class PasswordFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  @Input()
  okLabel = 'Add';
  @Input()
  user?: User;
  @Output()
  onSubmit = new EventEmitter<UpdateCredentialsRequest>();
  form = this.formBuilder.group({
    oldPassword: this.formBuilder.control('', [Validators.required]),
    newPassword: this.formBuilder.group({
      password: new FormControl('', [Validators.required]),
      repeatPassword: new FormControl('', [Validators.required]),
    }, {validators: [samePassword('password', 'repeatPassword')]}),
  });

  submit() {
    const oldPassword = this.form.get('oldPassword')!.value!;
    const newPassword = this.form.get('newPassword')!.get('password')!.value!;
    this.onSubmit.emit({oldPassword, newPassword});
  }
}
