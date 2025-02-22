import {Component, computed, inject, input, model, OnChanges, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {UserService} from '../../services/user.service';
import {UpdateUserRequest} from '../../models/update-user-request';
import {User} from '../../models/user';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap, throttleTime} from 'rxjs';

@Component({
  selector: 'mediminder-profile-form',
  imports: [
    MatAutocomplete,
    MatAutocompleteTrigger,
    MatButton,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    FormsModule
  ],
  templateUrl: './profile-form.component.html',
  styleUrl: './profile-form.component.scss',
  standalone: true,
})
export class ProfileFormComponent implements OnChanges {
  private readonly userService = inject(UserService);
  okLabel = input('Add');
  user = input<User>();
  confirm = output<UpdateUserRequest>();

  name = model('');
  timezone = model('');
  timezones = toSignal(toObservable(this.timezone).pipe(
    throttleTime(300),
    mergeMap(search => this.userService.findAvailableTimezones(search || ''))
  ), {initialValue: []});
  isNameEmpty = computed(() => !this.name());
  isTimezoneNotFromList = computed(() => !this.timezones().includes(this.timezone()));
  request = computed<UpdateUserRequest>(() => ({
    name: this.name(),
    timezone: this.timezone(),
  }));

  ngOnChanges() {
    this.name.set(this.user()?.name || '');
    this.timezone.set(this.user()?.timezone || '');
  }
}
