import {Component, model, output} from '@angular/core';
import {MatFormField, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'mediminder-chat-form',
  imports: [
    MatFormField,
    MatInput,
    MatIconButton,
    MatSuffix,
    MatIcon,
    FormsModule,

  ],
  templateUrl: './chat-form.component.html',
  styleUrl: './chat-form.component.scss'
})
export class ChatFormComponent {
  message = model('');
  send = output<string>();

  onSubmit() {
    this.send.emit(this.message());
    this.message.set('');
  }
}
