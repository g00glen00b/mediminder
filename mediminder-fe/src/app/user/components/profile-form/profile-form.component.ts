import {Component, EventEmitter, inject, Input, OnChanges, Output} from '@angular/core';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {toSignal} from '@angular/core/rxjs-interop';
import {mergeMap, startWith, throttleTime} from 'rxjs';
import {UserService} from '../../services/user.service';
import {UpdateUserRequest} from '../../models/update-user-request';
import {User} from '../../models/user';

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
    ReactiveFormsModule
  ],
  templateUrl: './profile-form.component.html',
  styleUrl: './profile-form.component.scss',
  standalone: true,
})
export class ProfileFormComponent implements OnChanges {
  private readonly formBuilder = inject(FormBuilder);
  private readonly userService = inject(UserService);
  @Input()
  okLabel = 'Add';
  @Input()
  user?: User;
  @Output()
  onSubmit = new EventEmitter<UpdateUserRequest>();
  form = this.formBuilder.group({
    name: new FormControl('', Validators.maxLength(128)),
    timezone: new FormControl('', Validators.maxLength(64)),
  });
  timezones = toSignal(this.form.get('timezone')!.valueChanges.pipe(
    startWith(''),
    throttleTime(300),
    mergeMap(search => this.userService.findAvailableTimezones(search || ''))
  ), {initialValue: []});

  submit() {
    const timezone = this.form.get('timezone')!.value || undefined;
    const name = this.form.get('name')!.value || undefined;
    this.onSubmit.emit({timezone, name});
  }

  ngOnChanges() {
    this.form.patchValue({
      timezone: this.user?.timezone || 'UTC',
      name: this.user?.name
    });
  }
}
