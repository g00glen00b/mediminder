import {Component, computed, input, model, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {User} from '../../models/user';
import {UpdateCredentialsRequest} from '../../models/update-credentials-request';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';
import {SamePasswordDirective} from '../../directives/same-password.directive';

@Component({
  selector: 'mediminder-password-form',
  imports: [
    FormsModule,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatButton,
    SamePasswordDirective
  ],
  templateUrl: './password-form.component.html',
  styleUrl: './password-form.component.scss'
})
export class PasswordFormComponent {
  okLabel = input('Add');
  user = input<User>();
  confirm = output<UpdateCredentialsRequest>();

  oldPassword = model('');
  password = model('');
  repeatPassword = model('');
  request = computed<UpdateCredentialsRequest>(() => ({
    oldPassword: this.oldPassword(),
    newPassword: this.password()
  }));
}
